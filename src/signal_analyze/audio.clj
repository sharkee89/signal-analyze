(ns signal-analyze.audio
  (:import [javax.sound.sampled AudioSystem AudioFormat AudioInputStream AudioFileFormat$Type])
  (:require [clojure.java.io :as io]))

;; Čitanje WAV fajla
(defn read-audio [filename]
  (with-open [ais (AudioSystem/getAudioInputStream (io/file filename))]
    (let [format (.getFormat ais)
          frames (.available ais)
          buffer (byte-array frames)]
      (.read ais buffer 0 frames)
      (println "Audio format:" format)
      {:format format
       :data buffer})))

;; Konverzija stereo 16-bit PCM u float [-1.0, 1.0]
(defn bytes-to-floats [byte-array]
  (let [frames (quot (alength byte-array) 4)
        left  (float-array frames)
        right (float-array frames)]
    (dotimes [i frames]
      (let [idx (* i 4)
            l (short (bit-or (aget byte-array idx)
                             (bit-shift-left (aget byte-array (inc idx)) 8)))
            r (short (bit-or (aget byte-array (+ idx 2))
                             (bit-shift-left (aget byte-array (+ idx 3)) 8)))]
        (aset left i (/ l 32768.0))
        (aset right i (/ r 32768.0))))
    {:left left :right right}))

;; Sigurna konverzija float -> 16-bit PCM
(defn clamp [x min-val max-val]
  (max min-val (min max-val x)))

;; Konverzija float stereo u byte array (16-bit PCM, little-endian)
(defn floats-to-stereo-bytes [left right]
  (let [n (alength left)
        byte-array (byte-array (* 4 n))]
    (dotimes [i n]
      (let [l-sample (int (clamp (* (aget left i) 32767) -32768 32767))
            r-sample (int (clamp (* (aget right i) 32767) -32768 32767))]
        ;; levi kanal
        (aset byte-array (* 4 i) (byte (bit-and l-sample 0xFF)))                      ;; LSB
        (aset byte-array (+ (* 4 i) 1) (byte (bit-and (bit-shift-right l-sample 8) 0xFF))) ;; MSB
        ;; desni kanal
        (aset byte-array (+ (* 4 i) 2) (byte (bit-and r-sample 0xFF)))                      
        (aset byte-array (+ (* 4 i) 3) (byte (bit-and (bit-shift-right r-sample 8) 0xFF))))))
    byte-array)

;; Pisanje stereo WAV fajla
(defn write-stereo-wav [filename left right sample-rate]
  (let [n (alength left)
        bytes (floats-to-stereo-bytes left right)
        format (AudioFormat. (float sample-rate) 16 2 true false) ;; 2 kanala, signed, little-endian
        ais (AudioInputStream. (java.io.ByteArrayInputStream. bytes)
                               format n)]
    (AudioSystem/write ais AudioFileFormat$Type/WAVE (io/file filename))))

;; Opcionalna normalizacija: skalira sve float vrednosti u [-1, 1] da izbegne clipping
(defn normalize-floats [float-array]
  (let [n (alength float-array)
        max-abs (reduce (fn [acc i] (max acc (Math/abs (aget float-array i))))
                        0.0
                        (range n))]
    (if (zero? max-abs)
      float-array
      (let [scale (/ 1.0 max-abs)
            out (float-array n)]
        (dotimes [i n]
          (aset out i (* (aget float-array i) scale)))
        out))))
