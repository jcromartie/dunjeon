(ns dunjeon.security
  (:import java.security.MessageDigest java.util.UUID))

(defn uuid
  "Returns a random UUID"
  []
  (.toString (UUID/randomUUID)))

(defn- bytes-to-hex-str
  "Does what it says on the tin"
  [byte-array]
  (apply str (map #(format "%02x" (bit-and % 0xff)) byte-array)))

(defn- sha-hash
  "Returns the SHA hash of string s"
  [s]
  (bytes-to-hex-str
   (.digest
    (doto (MessageDigest/getInstance "SHA-1") (.update (.getBytes s))))))

(defn- salted-hash
  [s]
  (sha-hash (str "your salt here" s)))

(defn- object-token
  "Creates security token for a given object"
  [x hash-fn]
  (hash-fn (pr-str x)))

(def *token-key* :obj-hash)

(defn sign
  "Returns an object with a verifiable signature"
  [x]
  (with-meta x (assoc ^x *token-key* (object-token x salted-hash))))

(defn auth
  "Verifies the authenticity of a signed object"
  [x]
  (= (^x *token-key*) (object-token x salted-hash)))

(defn pr-signed-str
  "Returns the signed object in a readable format"
  [x]
  (let [signed (sign x)]
    (pr-str [(^signed *token-key*) signed])))

(defn pr-signed
  "Writes the an object with a signature in a readable format"
  [x]
  (print (pr-signed-str x)))

(defn read-signed-string
  "Returns an objecy by reading a string representation"
  [s]
  (let [v (read-string s)]
    (with-meta (v 1) {*token-key* (v 0)})))