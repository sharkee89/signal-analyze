(ns signal-analyze.nn
  (:require [uncomplicate.neanderthal.core :refer :all]
            [uncomplicate.neanderthal.native :refer :all]))

(defn ensure-dv [x]
  (if (sequential? x)
    (dv x)
    x))

(defn sigmoid [x]
  (/ 1.0 (+ 1.0 (Math/exp (- x)))))

(defn feed-forward [inputs weights1 bias1 weights2 bias2]
  (let [x (ensure-dv inputs)

        z1 (mv weights1 x)
        _  (axpy! 1.0 bias1 z1)

        hidden (dv (map sigmoid (copy z1)))

        y (+ (dot weights2 hidden) bias2)]
    y))

(defn mse [predicted actual]
  (let [errors (map #(- %1 %2) predicted actual)]
    (/ (reduce + (map #(Math/pow % 2) errors)) (count errors))))

(defn init-network [input-units hidden-units]
  (let [weights1 (dge hidden-units          ;; broj redova
                      input-units           ;; broj kolona
                      (repeatedly (* hidden-units input-units)
                                  #(rand-nth [-0.5 0.5]))
                      {:layout :row})
        bias1    (dv (repeatedly hidden-units #(rand-nth [-0.5 0.5])))
        weights2 (dv (repeatedly hidden-units #(rand-nth [-0.5 0.5])))
        bias2    0.1]
    {:weights1 weights1
     :bias1 bias1
     :weights2 weights2
     :bias2 bias2}))

(def training-data
  [{:inputs [100 3 10 2] :output 55000}
   {:inputs [80 2 8 1] :output 40000}
   {:inputs [120 4 12 3] :output 70000}
   {:inputs [90 3 11 2] :output 50000}])

(defn train-network [network data]
  (let [predictions (map #(feed-forward
                            (:inputs %)              ;; <-- ovo mora biti običan vektor
                            (:weights1 network)
                            (:bias1 network)
                            (:weights2 network)
                            (:bias2 network))
                         data)

        actuals (map :output data)
        error (mse predictions actuals)]
    (println "Trenutna MSE:" error)
    network))

(defn normalize-feature [values]
  (let [min-val (apply min values)
        max-val (apply max values)]
    (map #(double (/ (- % min-val) (- max-val min-val))) values)))

(defn normalize-data [data]
  (let [input-cols (apply map vector (map :inputs data))
        normalized-inputs (mapv normalize-feature input-cols)
        normalized-inputs-transposed (apply map vector normalized-inputs)
        normalized-outputs (normalize-feature (map :output data))]
    ;; vraća novi dataset sa normalizovanim ulazima i izlazima
    (mapv (fn [orig in out] {:inputs in :output out})
          data
          normalized-inputs-transposed
          normalized-outputs)))

(defn predict
  "Predviđa izlaz za novi unos koristeći mrežu i opseg trening podataka."
  [network input training-data]
  ;; Prvo normalizujemo ulaz po kolonama trening dataseta
  (let [input-cols (apply map vector (map :inputs training-data))
        min-vals (map #(apply min %) input-cols)
        max-vals (map #(apply max %) input-cols)
        normalize (fn [x min max] (/ (- x min) (- max min)))
        normalized-input (mapv normalize input min-vals max-vals)
        output (feed-forward normalized-input
                             (:weights1 network)
                             (:bias1 network)
                             (:weights2 network)
                             (:bias2 network))
        ;; izlaz takođe skaliran prema min/max originalnih izlaza
        outputs (map :output training-data)
        out-min (apply min outputs)
        out-max (apply max outputs)
        denormalized-output (+ (* output (- out-max out-min)) out-min)]
    denormalized-output))
