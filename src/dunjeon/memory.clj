(ns dunjeon.memory)

(defn bytes-used
  "Returns bytes used by Java VM (after collecting garbage)"
  []
  (let [rt (Runtime/getRuntime)]
    (dorun (take 5 (repeatedly #(System/gc))))
    (- (.totalMemory rt) (.freeMemory rt))))

(defmacro mime
  "Measures and prints the bytes used while evaluating expr (returns result of expr)"
  [expr]
  `(let [mem-start# (bytes-used)
	 result# (do ~expr)]
     (println (- (bytes-used) mem-start#) "bytes used")
     result#))
