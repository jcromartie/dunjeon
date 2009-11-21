(ns dunjeon.world
  (:use dunjeon.security dunjeon.dice))

(defmacro realm
  "Sets the current realm to name"
  [realm-name]
  `(ns ~realm-name (:use dunjeon.world)))

(defn str-match
  [s1 s2]
  (<= 0 (.indexOf (.toLowerCase s1) (.toLowerCase s2))))

(defn find-exit
  [room query]
  (let [query-str (str query)
	exit (first
	      (filter (fn [[k _]] (str-match (name k) query-str))
		      (:exits room)))]
    (when exit
      (key exit))))

(defn dest
  [room exit]
  (-> room :exits exit))

(defmacro room
  [id room-name desc & exits]
  `(def ~id
	(hash-map
	 :thing-type :room
	 :id (keyword (name '~id))
	 :name ~room-name
	 :desc ~desc
	 :exits (hash-map ~@exits))))

(defn load-realm
  [realm-name]
  (println "Loading" realm-name)
  (load-file (str "data/" realm-name ".clj")))

(defn describe
  [room]
  (println (:name room))
  (println (:desc room))
  (dorun (for [exit (:exits room)] (println "  " (name (key exit)) ": " (name (val exit))))))
