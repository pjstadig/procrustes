(ns procrustes.file
  (:use [clojure.java.io :only [file]])
  (:import (java.io File)))

(defn segments->file #^File [segments]
  (reduce (fn [file s]
            (File. file s))
          segments))

(defn file->segments
  ([#^File file]
     (file->segments file () (set (File/listRoots))))
  ([#^File file segments roots]
     (if (and file (not (roots file)))
       (recur (.getParentFile file) (conj segments (.getName file)) roots)
       (conj segments file))))

(defn common-prefix
  ([[root1 & segments1] [root2 & segments2]]
     (if (= root1 root2)
       (common-prefix segments1 segments2 [root1])
       []))
  ([[s1 & segments1] [s2 & segments2] result]
     (if (= s1 s2)
       (if (and (seq segments1) (seq segments2))
         (recur segments1 segments2 (conj result s1))
         (conj result s1))
       result)))

(defn common-ancestor
  ([files]
     (if (empty? files)
       nil
       (reduce common-ancestor files)))
  ([#^File file1 #^File file2]
     (let [prefix (common-prefix (file->segments file1)
                                 (file->segments file2))]
       (segments->file prefix))))

(defn canonicalize-file [file base]
  (.replaceAll (.getCanonicalPath file)
               (str (.getCanonicalPath base) "/?") ""))

(defn find-files [src-dirs test-dirs]
  (let [src-files (mapcat #(filter (fn [f]
                                     (and (memfn isFile)
                                          (re-find #"\.clj$" (.getName f))))
                                   (file-seq (.getCanonicalFile (file %))))
                          src-dirs)
        test-files (mapcat #(filter (fn [f]
                                      (and (memfn isFile)
                                           (re-find #"\.clj$" (.getName f))))
                                    (file-seq (.getCanonicalFile (file %))))
                           test-dirs)
        dirs (concat src-dirs test-dirs)
        base (if (= 1 (count dirs))
               (file (first dirs))
               (common-ancestor (map file dirs)))]
    [base src-files test-files]))
