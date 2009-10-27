(ns dunjeon.dice)

(defn roll
  "Rolls an n-sided die"
  [n]
  (inc (rand-int n)))

(defn aced-roll
  "Rolls an n-sided die with Savage Worlds acing. Returns a list of rolls"
  [n]
  (loop [this-roll (roll n)
	 rolls []]
    (if-not (= n this-roll)
      (conj rolls this-roll)
      (recur (roll n) (conj rolls this-roll)))))

(defn sum
  "Adds all items in coll"
  [coll]
  (apply + coll))
