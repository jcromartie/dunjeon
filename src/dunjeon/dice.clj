(ns net.cromstar.dunjeon.dice)

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
