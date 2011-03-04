(ns procrustes.line-count
  (:use [clojure.java.io :only [reader file]]
        [procrustes.file :only [common-ancestor canonicalize-file find-files]])
  (:import (java.io File)))

(defn code? [line]
  (let [line (.trim line)]
    (and (not= (.length line) 0)
         (not= (.charAt line 0) \;))))

(defn line-count [base file]
  {(canonicalize-file file base)
   (count (filter code?
                  (line-seq (reader file))))})

(defn line-counts
  ([src-dirs test-dirs]
     (apply line-counts (find-files src-dirs test-dirs)))
  ([base src-files test-files]
     (let [src-stats (reduce merge {} (map (partial line-count base)
                                           src-files))
           stats (if (empty? src-stats)
                   {}
                   {:file {:src src-stats}})
           test-stats (reduce merge {} (map (partial line-count base)
                                            test-files))]
       (if (empty? test-stats)
         stats
         (update-in stats [:file] assoc :test test-stats)))))
