(ns dunjeon.creature)

(defmulti health
  :archetype)

(defmethod health :default
  [_]
  1)

(defmethod health :goblin
  [g]
  (+ 3 (:level g)))

(defmulti describe
  :archetype)

(defmethod describe :default
  [c]
  (println (:name c)))

(defn make-creature
  "Makes a new instance of a creature based on the archetype"
  [archetype level]
  {:archetype archetype
   :name (str "a " (name archetype))
   :level level})

(defn name-creature
  [c new-name]
  (assoc c :name new-name))
