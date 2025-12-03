(ns signal-analyze.device
  (:require [uncomplicate.clojurecl.core :as cl]
            [uncomplicate.clojurecl.info :as info]))

    ;; Platforma i uređaj
    (def platform (first (cl/platforms)))
    (def device (first (cl/devices platform :gpu)))

    ;; Kreiraj kontekst
    (def ctx (cl/context [device]))

    ;; Kernel source
    (def src "__kernel void add(__global const float* a, __global const float* b, __global float* c) {
                 int gid = get_global_id(0);
                 c[gid] = a[gid] + b[gid];
               }")


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
      (println "---------------------------------------------------------------")))