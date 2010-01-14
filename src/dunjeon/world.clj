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
	exit (first (filter #(starts-with % query-str) (map name (-> room :exits keys))))]
    (when exit
      (keyword exit))))

(defn dest
  "Returns the desination id for an exit"
  [room exit]
  (get (:exits room) exit))

(defmacro room
  "Define a room template. The room template is defined as a var in the
current namespace based on the id, with the natural language name of
room-name, and a longer description desc. The rest of the arguments
should be keyword-symbol couplets linking exits to room id (optionally
namespace-qualified... e.g. town/town-square)."
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
