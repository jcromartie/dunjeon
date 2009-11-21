(ns dunjeon.dice)

(defn roll
  "Rolls an n-sided die"
  ([n] (inc (rand-int n)))
  ([times n] (apply + (take times (repeatedly #(roll n))))))

(defn aced-roll
  "Rolls an n-sided die with Savage Worlds acing. Returns a list of rolls"
  [n]
  (loop [this-roll (roll n)
	 rolls []]
    (let [result (conj rolls this-roll)]
      (if (= n this-roll)
	(recur (roll n) result)
	result))))
