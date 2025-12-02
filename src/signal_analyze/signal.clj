(ns signal-analyze.signal
  (:require
    [uncomplicate.neanderthal.core :refer :all]
    [uncomplicate.neanderthal.native :refer :all]))

(defn generate-signal [n]
  (dv (map #(+
              (Math/sin (/ (* 2 Math/PI %) 20))
              (* 0.1 (rand)))
           (range n))))

(defn create-filter []
  (dge 3 3 [1 1 1
            1 1 1
            1 1 1]))

(defn convolve [signal filter]
  (let [signal-vec (into [] signal)
        filter-vec (into [] (row filter 0))
        fsize (count filter-vec)
        n (count signal-vec)
        values (map (fn [i]
                      (reduce + (map * (subvec signal-vec i (+ i fsize)) filter-vec)))
                    (range (- n fsize)))]
    (dv (vec (concat values (repeat (- n (count values)) 0))))))
