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
    (on-thread #(accept-socket (. server-socket accept)))
    server-socket))

(def prompt " > ")

(defn game-repl
  "runs a game prompt"
  [in out]
  (binding [*ns* (create-ns 'mud)
	    *warn-on-reflection* false
	    *out* (OutputStreamWriter. out)]
    (let [eof (Object.)
	  r (BufferedReader. (InputStreamReader. in))]
	  (print prompt)
      (loop [line (. r readLine)]
	(when-not (= line "quit")
	  (println "Thanks for writing" line)
	  (print prompt)
	  (flush)
	  (recur (. r readLine)))))))

(defn -main
  (println "Thanks for playing"))