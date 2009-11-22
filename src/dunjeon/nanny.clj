(ns dunjeon.nanny
  (:use (dunjeon util)))

(defn- get-quest
  [session]
  (loop [quest (prompt "What is your quest?")]
    (cond
     (= quest "") (recur (prompt "Come on now... what's the quest?"))
     :else (dosync
	    (alter session assoc :quest quest)
	    :done))))

(defn- get-name
  [session]
  (loop [name (prompt "What is your name?")]
    (cond
     (= name "") (recur (prompt "Give me a proper name"))
     :else (dosync
	    (alter session assoc :name name)
	    get-quest))))

(defn nanny
  "Walks the player through session setup"
  [session]
  (println "Let's get started, shall we?")
  (loop [state get-name]
    (when-not (= state :done)
      (recur (state session)))))
