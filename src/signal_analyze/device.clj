(ns signal-analyze.device
  (:require [uncomplicate.clojurecl.core :as cl]
            [uncomplicate.clojurecl.info :as info]))

;; Platforma i uređaj
(def platform (first (cl/platforms)))
(def device (first (cl/devices platform :gpu)))

;; Kreiraj kontekst
(def ctx (cl/context [device]))
(def queue (cl/command-queue-1 ctx device))

;; Kernel source
(def src "__kernel void add(__global const float* a, __global const float* b, __global float* c) {
             int gid = get_global_id(0);
             c[gid] = a[gid] + b[gid];
           }")


(def program (cl/build-program! (cl/program-with-source ctx [src])))
(def kernel  (cl/kernel program "add"))

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

(defn print-device-info []
    (def pls (cl/platforms))
    (def p (first pls))
    (def gpus (cl/devices p))

    (doseq [d gpus]
      (println "Device type: " (info/device-type d))
      (println "Name:" (info/name-info d))
      (println "Vendor:" (info/vendor d))
      (println "Driver version:" (info/driver-version d))
      (println "Device version:" (info/device-version d))
      (println "---------------------------------------------------------------"))
  (println ctx)
  (println queue)
  (println kernel))