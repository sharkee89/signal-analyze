(ns signal-analyze.core
  (:require [uncomplicate.commons.core :refer [with-release]]
            [uncomplicate.neanderthal.native :refer :all]
            [signal-analyze.audio :as audio]
            [signal-analyze.dd-deep-learning-to-gpu :as dddltg]
            [signal-analyze.device :as device]
            [signal-analyze.fft :as fft]
            [signal-analyze.frequencies :as freq]
            [signal-analyze.linear-regression :as linear]
            [signal-analyze.nn :as nn]
            [signal-analyze.signal :as signal]
            [clojure.java.io :as io]))

(defn get-top-five-frequencies-and-notes-from-audio []
  (let [sample-rate 44100
        audio-data (audio/read-audio (io/resource "test.wav"))
        samples (audio/bytes-to-dv (:data audio-data))
        left-samples (:left samples)
        fft-left (fft/fftdv left-samples)
        amplitudes-left (fft/amplitude-spectrum fft-left)
        top5 (freq/dominant-frequencies amplitudes-left sample-rate 5)
        top5-notes (freq/annotate-frequencies-with-notes top5)]
    top5-notes))

(defn generate-and-filter-signal []
  (with-release [signal (signal/generate-signal 100)
                 filter (signal/create-filter)]
                (let [filtered (signal/convolve signal filter)]
                  {:signal signal :filter filter :filtered filtered})))

(defn predict-house-price []
  (let [area 100
        room_number 3
        location 10
        floor 2
        basePrice 50000
        priceCoeff 2000
        roomCoeff 5000
        locationCoeff 2000
        floorCoeff 3000]
  (linear/linear-regression
    basePrice
    [area room_number location floor]
    [priceCoeff roomCoeff locationCoeff floorCoeff])))

;; -----------------------------
;; CPU simple add
;; -----------------------------
(defn cpu-add [a b]
  (float-array (map + a b)))

;; CPU verzija compound_op (samo za poređenje)
(defn cpu-compound-add [a b]
  (float-array
    (map (fn [x y] (+ (Math/sqrt (+ (* x x) (* y y)))
                      (Math/sin x)
                      (- (Math/cos y))))
         a b)))

;; -----------------------------
;; Merenje vremena (ms)
;; -----------------------------
(defn measure [f]
  (let [start (System/nanoTime)]
    (f)
    (/ (double (- (System/nanoTime) start)) 1e6)))

;; -----------------------------
;; Benchmark za jednu veličinu vektora (GPU + CPU)
;; -----------------------------
(defn benchmark-size [n]
  (let [a (float-array (repeatedly n #(float (rand))))
        b (float-array (repeatedly n #(float (rand))))]

    {:n n
     ;; Simple add CPU vs GPU (original)
     :cpu-ms (measure #(cpu-add a b))
     :gpu-ms (measure #(vec (device/gpu-add a b)))

     ;; Compound_op CPU vs GPU
     :cpu-compound-ms (measure #(cpu-compound-add a b))
     :gpu-compound-ms (measure #(vec (device/gpu-compound-add a b)))}))

;; -----------------------------
;; Veličine vektora za test
;; -----------------------------
(def sizes [1000 10000 50000 100000 500000 1000000])

;; -----------------------------
;; Pokretanje benchmarka
;; -----------------------------
(defn run-benchmarks []
  (println "Running benchmarks (CPU vs GPU)...\n")
  (doseq [{:keys [n cpu-ms gpu-ms cpu-compound-ms gpu-compound-ms]} (map benchmark-size sizes)]
    ;; Original simple add
    (println (format "Simple add N = %-8d | CPU: %-10.3f ms | GPU: %-10.3f ms | Speedup: %.1fx"
                     n cpu-ms gpu-ms (/ cpu-ms gpu-ms)))
    ;; Compound_op
    (println (format "Compound_op N = %-8d | CPU: %-10.3f ms | GPU: %-10.3f ms | Speedup: %.1fx"
                     n cpu-compound-ms gpu-compound-ms (/ cpu-compound-ms gpu-compound-ms)))
    (println "---------------------------------------------------------------")))

(defn -main [& args]
  (run-benchmarks))