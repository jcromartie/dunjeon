(ns dunjeon.classes
  (:use dunjeon.creature))

(defmethod health ::fighter
  [p]
  (+ 5 (* 10 (:level p))))

(defmethod health ::mage
  [p]
  (+ 10 (* 3 (:level p))))

(defmethod health ::power-ranger
  [p]
  (+ 8 (* 8 (:level p))))
