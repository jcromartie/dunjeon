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
  "Find a room by keyword in the current realm for the given session"
  [session k]
  (let [room-var (ns-resolve (:realm session) (symbol (name k)))]
    (when room-var
      (deref room-var))))

(defmulti command
  "Process a session and command list"
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
    (describe (:room session))))

(defmethod command 'go
  [session [cmd exit-query]]
  (println "go =>" cmd exit-query)
  (if exit-query
    (let [room (:room session)
	  exit (find-exit room exit-query)
	  next-room (find-room session (dest room exit))]
      (if next-room
	(assoc session :room next-room)
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
  "Makes a new session in the town square"
  []
  {:realm (find-ns 'town), :room town/town-square})

(defn on-thread
  "runs f on a new thread"
  [f]
  (doto (Thread. f) (.start)))

(defn create-server
  "creates a server on port, passing accepted sockets to accept-socket"
  [port accept-socket]
  (let [server-socket (ServerSocket. port)]
    (on-thread
     #((loop
	   [client-socket (. server-socket accept)]
	 (accept-socket client-socket)
	 (recur (. server-socket accept)))))
    server-socket))

(defn prompt
  [session]
  (print (-> session :room :name) "> "))

(def intro "Welcome to dunjeon. Type quit to quit at any time.")

(defn game-repl
  "runs a game loop on the given in/out streams"
  [in out session]
  (binding [*warn-on-reflection* false
	    *out* (OutputStreamWriter. out)]
    (let [eof (Object.)
	  r (BufferedReader. (InputStreamReader. in))]
      (println intro)
      (prompt session)
      (flush)
      (loop [line (. r readLine) session session]
	(when-not (or (= line nil) (= line "quit"))
	  (let [line-list (try (read-string (str "(" line ")")) (catch Exception err nil))
		result (try (command session line-list) (catch Exception e (println "Oops" e)))]
	    (if result
	      (do
		(prompt result) (flush)
		(recur (. r readLine) result))
	      (do
		(prompt session) (flush)
		(recur (. r readLine) session)))))))))

(defn handle-game-client
  "handles client socket"
  [client]
  (on-thread #((do
		 (game-repl (. client getInputStream) (. client getOutputStream))
		 (. client close)))))

(defn local-game
  "runs game client locally on stdin/stdout"
  []
  (game-repl System/in System/out (make-session)))

(defn main-
  []
  (let [port (nth *command-line-args* 0)]
    (if (= port "local")
      (local-game)
      (do
	(def server (create-server (Integer. port) handle-game-client))
	(println "Server started on port" port)))))
