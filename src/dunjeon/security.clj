(ns dunjeon.security)

(defn create-token
  "creates security token"
  []
  "foobar")

(defn secure-object
  "generates and attaches a security token to ab object"
  []
  #^{:dunjeon-token (create-token)} {})

(defn validate-object
  "validates that a given object was created by this instance"
  [x]
  (= (:dunjeon-token ^x) (create-token)))