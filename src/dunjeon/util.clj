(ns dunjeon.util)

(def *stop-words*
     '#{in with at into from to of a the on in over})

(defn trim-stop-words
  "Trims stop words from symbol list x"
  [x]
  (remove *stop-words* x))

(defmacro on-thread
  "runs f on a new thread"
  [& exprs]
  `(let [thread# (Thread. #(do ~@exprs))]
     (.start thread#)
     thread#))

(defn prompt
  "Prints 'text >' and returns the next line from *in*"
  [text]
  (print text "> ")
  (flush)
  (read-line))

(defn starts-with
  "Returns if s1 starts with s2, case-insensitive"
  [s1 s2]
  (.startsWith (.toLowerCase s1) (.toLowerCase s2)))

(defn qualify-sym
  "fully qualifies sym if it is a sym and if it doesn't specify a ns"
  [sym]
  (if (symbol? sym)
    (if (namespace sym)
      sym
      (symbol (name (ns-name *ns*)) (name sym)))
    sym))
