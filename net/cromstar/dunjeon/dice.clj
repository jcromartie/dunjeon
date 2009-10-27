(defn roll
  "Rolls an n-sided die"
  [n]
  (inc (rand-int n)))

(defn aced-roll
  "Rolls an n-sided die with Savage Worlds acing. Returns a list of rolls"
  [n]
  (roll n))

(defn sum
  "Adds all items in coll"
  [coll]
  (apply + coll))

(def positive-integers (iterate inc 0))
(def evens (iterate (partial + 2) 2))
(def odds (iterate (partial + 2) 1))

(def fib-seq
     ((fn rfib [a b]
	(cons a (lazy-seq (rfib b (+ a b)))))
      0 1))
