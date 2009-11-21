(ns dunjeon.server
  (:use [dunjeon world]))

(import '(java.net Socket ServerSocket))
(import '(java.io OutputStreamWriter InputStreamReader BufferedReader))

(load-realm "town")

(def *stop-words*
     '#{in with at into from to of a the on in over})

(defn trim-stop-words
  "Trims stop words from symbol list x"
  [x]
  (remove *stop-words* x))

(defn find-room
  "Find a room by keyword in the current realm for the given session ref"
  [session k]
  (let [room-var (ns-resolve (:realm @session) (symbol (name k)))]
    (when room-var
      (deref room-var))))

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
    (describe (:room @session))))

(defmethod command 'go
  [session [cmd exit-query]]
  (println "go =>" cmd exit-query)
  (if exit-query
    (let [room (:room @session)
	  exit (find-exit room exit-query)
	  next-room (find-room session (dest room exit))]
      (if next-room
	(dosync
	 (alter session assoc :room next-room))
	(println "You can't go that way")))
    (println "where, exactly?")))

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
	:room town/town-square
	:in in
	:out out)))

(defmacro on-thread
  "runs f on a new thread"
  [& exprs]
  `(let [thread# (Thread. #(do ~@exprs))]
     (.start thread#)
     thread#))

(defn create-server
  "creates a server on port, passing accepted sockets to accept-socket"
  [port accept-socket-fn]
  (let [server { :socket (ServerSocket. port) :players (ref #{}) }]
    (on-thread
     (loop
	   [client-socket (. (:socket server) accept)]
	 (accept-socket-fn client-socket server)
	 (recur (. (:socket server) accept))))
    server))

(defn prompt
  [session]
  (print (-> @session :room :name) "> "))

(def intro "Welcome to dunjeon. Type quit to quit at any time.")

(defn game-repl
  "runs a game loop on the given session ref's streams"
  [session]
  (binding [*warn-on-reflection* false
	    *out* (OutputStreamWriter. (:out @session))]
    (let [eof (Object.)
	  r (BufferedReader. (InputStreamReader. (:in @session)))]
      (println intro)
      (prompt session)
      (flush)
      (loop [line (. r readLine)]
	(when-not (or (= line nil) (= line "quit"))
	  (let [line-list (try (read-string (str "(" line ")")) (catch Exception err nil))]
	    (try
	     (command session line-list)
	     (catch Exception e (println "Oops" e)))
	    (prompt session) (flush)
	    (recur (. r readLine))))))))

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
	(def server (create-server (Integer. port) handle-client-connect))
	(println "Server started on port" port)))))
