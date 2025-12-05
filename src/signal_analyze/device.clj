(ns signal-analyze.device
  (:require [uncomplicate.clojurecl.core :as cl]
            [uncomplicate.clojurecl.info :as info]))

;; Platforma i uređaj
(def platform (first (cl/platforms)))
(def device (first (cl/devices platform :gpu)))

;; Kreiraj kontekst
(def ctx (cl/context [device]))
(def queue (cl/command-queue-1 ctx device))

;; Kernel source - sabiranje (ostaje ako želiš jednostavan kernel)
(def src "__kernel void add(__global const float* a, __global const float* b, __global float* c) {
             int gid = get_global_id(0);
             c[gid] = a[gid] + b[gid];
           }")

;; Kernel source - compound_op (više operacija po elementu)
(def src-1 "__kernel void compound_op(__global const float* a,
                                    __global const float* b,
                                    __global float* c) {
                  int gid = get_global_id(0);
                  float x = a[gid];
                  float y = b[gid];

                  // više operacija po elementu
                  float val = sqrt(x*x + y*y) + sin(x) - cos(y);

                  c[gid] = val;
                }")

;; Build compound_op kernel
(def program (cl/build-program! (cl/program-with-source ctx [src-1])))
(def kernel  (cl/kernel program "compound_op"))  ;; << ovde je ispravljeno

(defn gpu-add [arr-a arr-b]
  (let [n (count arr-a)
        buf-a (cl/cl-buffer ctx (* n 4) :read-only)
        buf-b (cl/cl-buffer ctx (* n 4) :read-only)
        buf-c (cl/cl-buffer ctx (* n 4) :write-only)]

    ;; Copy host → GPU buffers
    (cl/enq-write! queue buf-a arr-a)
    (cl/enq-write! queue buf-b arr-b)

    ;; Set kernel arguments
    (cl/set-args! kernel buf-a buf-b buf-c)

    ;; Execute kernel on N items
    (cl/enq-kernel! queue kernel (cl/work-size [n]))

    ;; Allocate result array
    (let [result (float-array n)]
      ;; Copy GPU → host
      (cl/enq-read! queue buf-c result)
      (cl/finish! queue)
      result)))

;; GPU funkcija koja koristi compound_op kernel
(defn gpu-compound-add [arr-a arr-b]
  (let [n (count arr-a)
        buf-a (cl/cl-buffer ctx (* n 4) :read-only)
        buf-b (cl/cl-buffer ctx (* n 4) :read-only)
        buf-c (cl/cl-buffer ctx (* n 4) :write-only)]

    ;; Copy host → GPU buffers
    (cl/enq-write! queue buf-a arr-a)
    (cl/enq-write! queue buf-b arr-b)

    ;; Set kernel arguments
    (cl/set-args! kernel buf-a buf-b buf-c)

    ;; Execute kernel on N items
    (cl/enq-kernel! queue kernel (cl/work-size [n]))

    ;; Allocate result array
    (let [result (float-array n)]
      ;; Copy GPU → host
      (cl/enq-read! queue buf-c result)
      (cl/finish! queue)
      result)))

;; Funkcija za ispis informacija o uređaju
(defn print-device-info []
  (let [pls (cl/platforms)
        p (first pls)
        gpus (cl/devices p)]
    (doseq [d gpus]
      (println "Device type: " (info/device-type d))
      (println "Name:" (info/name-info d))
      (println "Vendor:" (info/vendor d))
      (println "Driver version:" (info/driver-version d))
      (println "Device version:" (info/device-version d))
      (println "---------------------------------------------------------------")))
  (println ctx)
  (println queue)
  (println kernel))
