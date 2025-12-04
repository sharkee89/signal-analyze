(ns signal-analyze.dd-deep-learning-to-gpu
  (:require [uncomplicate.commons.core :refer [with-release]]
            [uncomplicate.fluokitten.core :refer [fmap!]]
            [uncomplicate.neanderthal.core :refer [mv! mv axpy! scal!]]
            [uncomplicate.neanderthal.native :refer [dv dge]]
            [uncomplicate.neanderthal.math :refer [signum exp]]
            [uncomplicate.neanderthal.vect-math :refer [fmax! tanh! linear-frac!]]))

(defn example-1-1 []
  (with-release [x (dv 0.3 0.9)
                 w1 (dge 4 2 [0.3 0.6
                              0.1 2.0
                              0.9 3.7
                              0.0 1.0]
                         {:layout :row})
                 h1 (dv 4)]
                (println (mv! w1 x h1))))

(defn example-1-2 []
  (with-release [x (dv 0.3 0.9)
                 w1 (dge 4 2 [0.3 0.6
                              0.1 2.0
                              0.9 3.7
                              0.0 1.0]
                         {:layout :row})
                 h1 (dv 4)
                 w2 (dge 1 4 [0.75 0.15 0.22 0.33])
                 y (dv 1)]
                (println (mv! w2 (mv! w1 x h1) y))))

(defn step!
  "Element-wise step function: signum(x - threshold) with maximum logic"
  [threshold x]
  (fmap! (fn [t xi] (signum (- (max t xi) t)))
         threshold x))

(let [threshold (dv [1 2 3])
      x (dv [0 2 7])]
  (step! threshold x))

(def x (dv 0.3 0.9))
(def w1 (dge 4 2 [0.3 0.6
                  0.1 2.0
                  0.9 3.7
                  0.0 1.0]
             {:layout :row}))
(def threshold (dv 0.7 0.2 1.1 2))

(defn example-2-1 []
  (let [x (dv [0.3 0.9])
        w1 (dge 4 2 [0.3 0.6
                     0.1 2.0
                     0.9 3.7
                     0.0 1.0]
                {:layout :row})
        threshold (dv [0.7 0.2 1.1 2])
        y (mv w1 x)]   ;; matriks-vektor množenje
    (step! threshold y))) ;; vraća rezultat step funkcije

(def bias (dv 0.7 0.2 1.1 2))
(def zero (dv 4))

(defn example-2-2 []
  (step! zero (axpy! -1.0 bias (mv w1 x))))

(defn relu! [threshold x]
  ;; Element-wise ReLU: max(0, x - threshold)
  (fmap! (fn [t xi] (max 0.0 (- xi t)))
         threshold x))

(defn example-2-3 []
  (relu! bias (mv w1 x)))

(defn example-2-4 []
  (with-release [y (dv 4)] ;; allocate vector for mv result
                ;; y = w1 * x - bias
                (mv! w1 x y)
                (axpy! -1.0 bias y)    ;; y = y - bias
                ;; element-wise tanh without using tanh! from vect-math
                (fmap! #(Math/tanh %) y)))