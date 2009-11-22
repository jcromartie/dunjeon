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
