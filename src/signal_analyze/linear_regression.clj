(ns signal-analyze.linear-regression
  (:require [uncomplicate.neanderthal.core :refer :all]
            [uncomplicate.neanderthal.native :refer :all]))

(defn linear-regression
  "Predviđa vrednost na osnovu zbira a0 i dva vektora iste dužine:
   - xs: vrednosti faktora (npr. povrsina, broj soba)
   - coeffs: koeficijenti (npr. a1, a2, ...)
   Vraća a0 + dot-product(xs, coeffs)"
  [a0 xs coeffs]
  (when (not= (count xs) (count coeffs))
    (throw (ex-info "Vektori xs i coeffs moraju biti iste duzine" {:xs xs :coeffs coeffs})))
  (+ a0 (dot (dv (vec xs))  (dv (vec coeffs)) )))