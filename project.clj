(defproject procrustes "1.0.0-SNAPSHOT"
  :description "Metrics for your Clojure code."
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [robert/hooke "1.0.2"]]
  :dev-dependencies [[swank-clojure "1.2.1"]
                     [lein-difftest "1.3.1-SNAPSHOT"]]
  :eval-in-project true
  :hooks [leiningen.hooks.difftest
          leiningen.metrics])
