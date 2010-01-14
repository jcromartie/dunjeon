(ns dunjeon.nanny
  (:use (dunjeon util)))

(defn- attr-getter
  "Prompts user for an attribute named by key, testing the value with pred"
  ([key p1 p2 pred]
     (fn [session]
       (loop [x (prompt p1)]
	 (if (pred x)
	   (dosync (alter session assoc key x))
	   (recur (prompt p2))))))
  ([key p1 p2]
     (attr-getter key p1 p2 #(not (= % "")))))

(def steps
     [
      (attr-getter :name "What is your name?" "Give me a proper name")
      (attr-getter :quest "What is your quest?" "Come on now, what's your quest?")
      (attr-getter :color "What is your favorite color?" "Red, green, anything?")
      (let [q "What is 1 + 1"]
	(attr-getter :captcha q q #(= (read-string %) 2)))
      ])

(defn nanny
  "Walks the player through session setup"
  [session]
  (println "Let's get started, shall we?")
  (doseq [step steps] (step session)))
