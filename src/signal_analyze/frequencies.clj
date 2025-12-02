(ns signal-analyze.frequencies)

(def note-names ["C" "C#" "D" "D#" "E" "F" "F#" "G" "G#" "A" "A#" "B"])

(defn dominant-frequencies
  [amplitudes sample-rate top-n]
  (let [n (alength amplitudes)
        freqs (map #(hash-map
                      :freq (double (* % (/ (double sample-rate) n)))
                      :amplitude (aget amplitudes %))
                   (range (quot n 2)))]
    (take top-n (sort-by :amplitude > freqs))))

(defn freq-to-note [freq]
  (let [a4 440.0
        n (Math/round (* 12 (/ (Math/log (/ freq a4)) (Math/log 2))))
        note-idx (mod (+ n 9) 12)
        octave (int (+ 4 (/ (+ n 9) 12)))]
    (str (note-names note-idx) octave)))

(defn annotate-frequencies-with-notes [freqs]
  (map #(assoc % :note (freq-to-note (:freq %))) freqs))
