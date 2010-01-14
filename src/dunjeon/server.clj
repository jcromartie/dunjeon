(ns dunjeon.server
  (:use [dunjeon util crypto world nanny]))

(import '(java.net Socket ServerSocket))
(import '(java.io OutputStreamWriter InputStreamReader BufferedReader))

; the only default area
(load-realm "town")

(defn reload
  []
  (doseq [ns (all-ns)]
    (try
     (use (ns-name ns) :reload)
     (catch Exception e nil))))

(defn get-room
  "Resolves and derefs the room identified by symbol room-sym"
  [room-sym]
  (if-let [room-var (resolve room-sym)]
    (deref room-var)))

(defn current-room
  [session]
  (get-room (last (:history @session))))

(defn broadcast
  [server msg]
  (doseq [session (-> server :players deref)]
    (binding [*out* (:out @session)]
      (println msg))))

(comment
  commands come in as lines of text, and are dispatched
  based on the first word in the line, with the rest of the line
  being passed onto the actual command)

(defn dispatch-command
  "dispatches command line string"
  [server session line]
  (keyword (first (.split line " "))))

(defmulti command
  "Process a session ref and command list"
  dispatch-command)

(defmethod command :default
  ; search for the cmd
  [server session line]
  (command server session (str "go " line)))

(defmethod command :look
  ; look at target, or at the current room
  [server session line]
  (describe (current-room session)))

(defmethod command :go
  [server session line]
  (if-let [exit-query (second (.split line " "))]
    (let [room (current-room session)
	  exit (find-exit room exit-query)
	  next-room (dest room exit)]
      (if (and next-room (get-room next-room))
	(do
	  (println "heading" (name exit))
	  (if-not (some #{next-room} (:history @session))
	    (describe (get-room next-room)))
	  (dosync (alter session assoc :history (conj (:history @session) next-room))))
	(println "You can't go that way")))
    (println "where, exactly?")))

(defmethod command :history
  [_ session _]
  (let [room-names (map :name (map get-room (:history @session)))]
    (println (reduce #(str %1 " -> " %2) room-names))))

(defmethod command :say
  [server session line]
  (broadcast server (str (:name @session) " said " line)))

(defmethod command :help
  [_ _ _]
  (println "Ha ha ha..."))

(defmethod command :commands
  [_ _ _]
  (println "
Comands are:
  look [what]
  go [where]
  history
  commands
  quit
  help
"))

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
	(try
	 (command server session line)
	 (catch Exception e
	   (println "Oops...")
	   (.printStackTrace e)))
	(recur (prompt (:name (current-room session))))))))

(defn make-session
  "Makes a new session ref with given streams in the town square"
  [in out]
  (ref (hash-map
	:realm (find-ns 'town)
	:history ['town/town-square]
	:in (BufferedReader. (InputStreamReader. in))
	:out (OutputStreamWriter. out))))

(defn handle-client-connect
  "handles client socket and sets up a game repl"
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

(defn main-
  []
  (let [port (nth *command-line-args* 0)]
    (if (= port "local")
      (local-game)
      (do
	(def *main-server* (make-server (Integer. port)))
	(println "Server started on port" port)))))

(comment
  To start a server at a REPL, run something like the following expression
  
    (def *server* (make-server 5000))
  
  Then you can telnet to localhost at port 5000 from another shell

    telnet localhost 5000

  It's just that simple!)