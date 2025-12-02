(ns signal-analyze.core
  (:require [uncomplicate.commons.core :refer [with-release]]
    [signal-analyze.audio :as audio]
    [signal-analyze.fft :as fft]
    [signal-analyze.frequencies :as freq]
    [signal-analyze.signal :as signal]
    [clojure.java.io :as io]))

(defn getTop5FrequenciesFromAudio []
  (def sample-rate 44100)
  (def audio-data (audio/read-audio (io/resource "test.wav")))
  (def samples (audio/bytes-to-floats (:data audio-data)))
  (def left-samples (:left samples))
  (def fft-left (fft/fft left-samples))
  (def amplitudes-left (fft/amplitude-spectrum fft-left))
  (def top5 (freq/dominant-frequencies amplitudes-left sample-rate 5))
  (def top5-notes (freq/annotate-frequencies-with-notes top5))
  (println "Top 5 frekvencija sa notama:" top5-notes))

(defn generateAndFilterSignal []
  (with-release [signal (signal/generate-signal 50)
                 filter (signal/create-filter)]
                (let [filtered (signal/convolve signal filter)]
                  (println signal)
                  (println filter)
                  (println filtered)))
  )

(defn -main [& args]
    (generateAndFilterSignal)
    (getTop5FrequenciesFromAudio)
  )