(defproject signal-analyze "0.1.0-SNAPSHOT"
  :description "Signal analysis project with linear algebra"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.12.3"]
                 [org.uncomplicate/neanderthal-base "0.60.0"]
                 [org.uncomplicate/neanderthal-openblas "0.60.0"]
                 [org.bytedeco/openblas "0.3.30-1.5.12" :classifier "macosx-x86_64"]
                 [com.github.wendykierp/JTransforms "3.1"]]

  :main signal-analyze.core
  :jvm-opts ^:replace ["-Dclojure.compiler.direct-linking=true"
                       "--enable-native-access=ALL-UNNAMED"])