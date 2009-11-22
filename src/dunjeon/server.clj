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

(defn current-room
  [session]
  (last (:history @session)))

(defn find-room
  "Find a room by keyword in the current realm for the given session ref"
  [session id]
  (find-room-in-realm (:realm @session) id))

(defmulti command
  "Process a session ref and command list"
  (fn [session [cmd args]] cmd))

(defmethod command :default
  ; search for the cmd
  [session [query]]
  (command session (list 'go query)))

(defmethod command 'look
  ; look at target, or at the current room
  [session [_ target]]
  (if target
    (println "Sorry, I can't do that yet.")
    (describe (current-room session))))

(defmethod command 'go
  [session [cmd exit-query]]
  (if exit-query
    (let [room (current-room session)
	  exit (find-exit room exit-query)
	  next-room (find-room session (dest room exit))]
      (if next-room
	(dosync
	 (println "heading to the" (name exit))
	 (alter session assoc :history (conj (:history @session) next-room)))
	(println "You can't go that way")))
    (println "where, exactly?")))

(defmethod command 'history
  [session _]
  (println (map :name (:history @session))))

(defmethod command 'commands
  [session _]
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
	:history [town/town-square]
	:in (BufferedReader. (InputStreamReader. in))
	:out (OutputStreamWriter. out))))

(defn make-server
  "creates a server on port, passing accepted sockets to accept-socket"
  [port accept-socket-fn]
  (let [server { :socket (ServerSocket. port) :players (ref #{}) }]
    (on-thread
     (loop
	   [client-socket (. (:socket server) accept)]
	 (accept-socket-fn client-socket server)
	 (recur (. (:socket server) accept))))
    server))

(def intro "Welcome to dunjeon. Type quit to quit at any time.")

(defn game-repl
  "runs a game loop on the given session ref's streams"
  [session]
  (binding [*warn-on-reflection* false
	    *out* (:out @session)
	    *in* (:in @session)]
    (println intro)
    (nanny session)
    (loop [line (prompt "Welcome to the Town Square")]
      (when-not (or (= line nil) (= line "quit"))
	(let [line-list (try (read-string (str "(" line ")")) (catch Exception err nil))]
	  (try
	   (command session line-list)
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
		 (game-repl session)
		 (.close client-socket)
		 (dosync
		  (alter (:players server) disj session))))))

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
