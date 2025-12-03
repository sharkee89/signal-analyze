(ns signal-analyze.core
  (:require [uncomplicate.commons.core :refer [with-release]]
            [uncomplicate.neanderthal.native :refer :all]
            [uncomplicate.clojurecl.core :as cl]
            [signal-analyze.audio :as audio]
            [signal-analyze.device :as device]
            [signal-analyze.fft :as fft]
            [signal-analyze.frequencies :as freq]
            [signal-analyze.linear-regression :as linear]
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
        age 1
        floor 2
        basePrice 50000
        priceCoeff 2000
        roomCoeff 5000
        locationCoeff 2000
        ageCoeff -1000
        floorCoeff 3000]
  (linear/linear-regression
    basePrice
    [area room_number location age floor]
    [priceCoeff roomCoeff locationCoeff ageCoeff floorCoeff])))

(defn -main [& args]
  (println (generate-and-filter-signal))
  (println "Top 5 frekvencija sa notama:" (get-top-five-frequencies-and-notes-from-audio))
  (println "Predviđena cena kuće je:" (predict-house-price))
)