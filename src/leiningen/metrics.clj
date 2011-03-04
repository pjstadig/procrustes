(ns leiningen.metrics
  (:use [clojure.pprint :only [pprint]]
        [procrustes.line-count :only [line-counts]]
        [procrustes.unused-names :only [unused-names]]
        [clojure.java.io :only [file writer]])
  (:import [java.io PushbackReader]
           [java.util Date]
           [java.text SimpleDateFormat]))

(defn- metrics-dir [project]
  (let [f (file (.getParent (file (:source-path project)))
                ".metrics"
                (.format (SimpleDateFormat. "yyyyMMddhhmmss") (Date.)))]
    (when-not (.exists f)
      (.mkdirs f))
    f))

(defn write-metrics [dir metrics]
  (doseq [[report metric] metrics]
    (let [path (file dir (str (name report) ".clj"))]
      (.mkdirs (.getParentFile path))
      (with-open [f (writer path)]
        (.write f (with-out-str (pprint metric)))))))

(defn- usage []
  (println "Try lein metrics line-counts"))

(defn metrics-command [project command args]
  (let [project-path (file (:source-path project))
        files (filter #(.endsWith (.getName %) ".clj")
                      (file-seq project-path))]
    (cond
     (= "line-counts" command) {:line-counts
                                (line-counts [(:source-path project)]
                                             [(:test-path project)])}
     (= "unused-names" command) {:unused-names
                                 (unused-names [(:source-path project)]
                                               [(:test-path project)])}
     (empty? command) {:line-counts (line-counts [(:source-path project)]
                                                 [(:test-path project)])
                       :unused-names (unused-names [(:source-path project)]
                                                   [(:test-path project)])}
     :else (println "Unknown command"))))

(defn gather [project command args]
  (write-metrics (metrics-dir project)
                 (metrics-command project command args)))

(defn metrics
  "Gather metrics about your codez."
  [project & [command & args]]
  (gather project command args))
