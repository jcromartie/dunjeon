(ns dunjeon.server
  (:use [dunjeon util world nanny]))

(import '(java.net Socket ServerSocket))
(import '(java.io OutputStreamWriter InputStreamReader BufferedReader))

; the only default area
(load-realm "town")

(defn reload
  "reloads major portions of system"
  []
  (use '(dunjeon util world nanny server) :reload))

(defn get-room
  "Resolves and derefs the room identified by symbol room-sym"
  [room-sym]
  (deref (resolve room-sym)))

(defn current-room
  [session]
  (get-room (last (:history @session))))

(defn broadcast
  [server msg]
  (doseq [player @(:players server)]
    (println player)))

(defmulti command
  "Process a session ref and command list"
  (fn [server session [cmd args]] cmd))

(defmethod command :default
  ; search for the cmd
  [server session [query]]
  (command server session (list 'go query)))

(defmethod command 'look
  ; look at target, or at the current room
  [_ session [_ target]]
  (if target
    (println "Sorry, I can't do that yet.")
    (describe (current-room session))))

(defmethod command 'go
  [server session [cmd exit-query]]
  (if exit-query
    (let [room (current-room session)
	  exit (find-exit room exit-query)
	  next-room (dest room exit)]
      (if next-room
	(dosync
	 (println "heading to the" (name exit))
	 (alter session assoc :history (conj (:history @session) next-room)))
	(println "You can't go that way")))
    (println "where, exactly?")))

(defmethod command 'history
  [_ session _]
  (let [room-names (map :name (map get-room (:history @session)))]
    (println (reduce #(str %1 " -> " %2) room-names))))

(defmethod command 'commands
  [_ session _]
  (println "
Comands are:
  look [what]
  go [where]
  commands"))

(defn make-session
  "Makes a new session ref with given streams in the town square"
  [in out]
  (ref (hash-map
	:realm (find-ns 'town)
	:history ['town/town-square]
	:in (BufferedReader. (InputStreamReader. in))
	:out (OutputStreamWriter. out))))

(def intro "Welcome to dunjeon. Type quit to quit at any time.")

(defn game-repl
  "runs a game loop on the given session ref's streams"
  [server session]
  (binding [*warn-on-reflection* false
	    *out* (:out @session)
	    *in* (:in @session)]
    (println intro)
    (nanny session)
    (loop [line (prompt "Welcome to the Town Square")]
      (when-not (or (= line nil) (= line "quit"))
	(let [line-list (try (read-string (str "(" line ")")) (catch Exception err nil))]
	  (try
	   (command server session line-list)
	   (catch Exception e
	     (println "Oops...")
	     (.printStackTrace e)))
	  (recur (prompt (:name (current-room session)))))))))

(defn handle-client-connect
  "handles client socket"
  [client-socket server]
  (let [in (.getInputStream client-socket)
	out (.getOutputStream client-socket)
	session (make-session in out)]
    (dosync
     (alter (:players server) conj session))
    (on-thread (do
		 (game-repl server session)
		 (.close client-socket)
		 (dosync
		  (alter (:players server) disj session))))))

(defn make-server
  "creates a server on port, passing accepted sockets to accept-socket"
  [port]
  (let [server { :socket (ServerSocket. port) :players (ref #{}) }]
    (on-thread
     (loop
	   [client-socket (. (:socket server) accept)]
	 (handle-client-connect client-socket server)
	 (recur (. (:socket server) accept))))
    server))

(defn local-game
  "runs game client locally on stdin/stdout"
  []
  (game-repl (make-session System/in System/out)))

(defn main-
  []
  (let [port (nth *command-line-args* 0)]
    (if (= port "local")
      (local-game)
      (do
	(def *main-server* (make-server (Integer. port) handle-client-connect))
	(println "Server started on port" port)))))
