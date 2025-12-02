(ns signal-analyze.fft
  (:import [org.jtransforms.fft DoubleFFT_1D]))

;; Konvertuje float[] u double[]
(defn float-to-double-array [float-array]
  (let [n (alength float-array)
        d (double-array n)]
    (dotimes [i n]
      (aset d i (double (aget float-array i))))
    d))

;; FFT funkcija
(defn fft [samples]
  (let [samples-d (float-to-double-array samples)
        n (alength samples-d)
        data (double-array (* 2 n))]
    (dotimes [i n]
      (aset data (* 2 i) (aget samples-d i))
      (aset data (+ (* 2 i) 1) 0.0))
    (let [fft-instance (DoubleFFT_1D. n)]
      (.complexForward fft-instance data))
    data))

;; Amplitudni spektar
(defn amplitude-spectrum [fft-data]
  (let [n (/ (count fft-data) 2)
        amplitudes (double-array n)]
    (dotimes [i n]
      (aset amplitudes i
            (Math/sqrt (+ (Math/pow (aget fft-data (* 2 i)) 2)
                          (Math/pow (aget fft-data (+ (* 2 i) 1)) 2)))))
    amplitudes))

;; Inverse FFT funkcija
(defn ifft [fft-data]
  (let [n (/ (alength fft-data) 2)
        data (double-array (alength fft-data))]
    ;; Kopiramo FFT podatke da ne menjamo original
    (System/arraycopy fft-data 0 data 0 (alength fft-data))
    ;; Pokrenemo inverse FFT
    (let [fft-instance (DoubleFFT_1D. n)]
      (.complexInverse fft-instance data true)) ;; true -> normalize
    ;; Izvucemo samo realne vrednosti
    (let [reals (float-array n)]
      (dotimes [i n]
        (aset reals i (float (aget data (* 2 i)))))
      reals)))
