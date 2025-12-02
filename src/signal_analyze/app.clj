(ns signal-analyze.app
  (:require [uncomplicate.neanderthal.core :refer :all]
            [uncomplicate.neanderthal.native :refer :all]))

;; Create matrices
(def a (dge 2 3 [1 2 3 4 5 6]))
(def b (dge 3 2 [1 3 5 7 9 11]))

;; Multiply matrices
(println (mm a b))
(println "Neanderthal works!")
