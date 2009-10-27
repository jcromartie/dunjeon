(ns net.cromstar.dunjeon.server)

(import '(java.net Socket ServerSocket))
(import '(java.io OutputStreamWriter InputStreamReader BufferedReader))

(defn on-thread
  "runs f on a new thread"
  [f]
  (doto (Thread. f) (.start)))

(defn create-server
  "creates a server on port, passing accepted sockets to accept-socket"
  [port accept-socket]
  (let [server-socket (ServerSocket. port)]
    (on-thread #((loop
		     [client-socket (. server-socket accept)]
		   (accept-socket client-socket)
		   (recur (. server-socket accept)))))
    server-socket))

(def prompt " > ")
(def intro "Welcome to dunjeon. Type quit to quit at any time.
You can't do anything right now.")

(defn game-repl
  "runs a game loop on the given in/out streams"
  [in out]
  (binding [*ns* (create-ns 'mud)
	    *warn-on-reflection* false
	    *out* (OutputStreamWriter. out)]
    (let [eof (Object.)
	  r (BufferedReader. (InputStreamReader. in))]
          (println intro) (print prompt) (flush)
      (loop [line (. r readLine)]
	(when-not (or (= line nil) (= line "quit"))
	  (print "Thanks for writing" (prn-str line)) (print prompt) (flush)
	  (recur (. r readLine)))))))

(defn handle-game-client
  "handles client socket"
  [client]
  (on-thread #((do
		 (game-repl (. client getInputStream) (. client getOutputStream))
		 (. client close)))))

(defn local-game-client
  "runs game client locally on stdin/stdout"
  []
  (game-repl System/in System/out))

(defn main-
  []
  (let [port (nth *command-line-args* 0)]
    (if (= port "local")
      (local-game-client)
      (do
	(def server (create-server (Integer. port) handle-game-client))
	(println "Server started on port" port)))))

(println (roll 5))