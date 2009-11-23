(ns dunjeon.world
  (:use dunjeon.util))

(defmacro realm
  "Sets the current realm to name"
  [realm-name]
  `(ns ~realm-name (:use dunjeon.world)))

(defn find-exit
  "Returns the first matching exit name for the query"
  [room query]
  (let [query-str (str query)
	exit (first
	      (filter (fn [[k _]] (str-match (name k) query-str))
		      (:exits room)))]
    (when exit
      (key exit))))

(defn dest
  "Returns the desination id for an exit"
  [room exit]
  (get (:exits room) exit))

(defn qualify-sym
  "fully qualifies sym if it is a sym and if it doesn't specify a ns"
  [sym]
  (if (symbol? sym)
    (if (namespace sym)
      sym
      (symbol (name (ns-name *ns*)) (name sym)))
    sym))

(defmacro room
  [id room-name desc & exits]
  `(def ~id
	(hash-map
	 :thing-type :room
	 :id (keyword (name '~id))
	 :name ~room-name
	 :desc ~desc
	 :exits (apply hash-map (map qualify-sym (quote (~@exits)))))))

(defn load-realm
  [realm-name]
  (println "Loading" realm-name)
  (load-file (str "data/" realm-name ".clj")))

(defn describe
  [room]
  (println (:desc room))
  (dorun (for [exit (:exits room)] (println "  " (name (key exit)) ": " (name (val exit))))))
