(ns procrustes.test.unused-names
  (:use [procrustes.unused-names] :reload)
  (:use [clojure.test]
        [procrustes.code :only [read-code]]))

(deftest test-unused-names
  (is (= '{:file {"src/unused_names/core.clj"
                  #{clojure.contrib.combinatorics cl
                    clojure.contrib.graph/add-loops}}}
         (unused-names ["fixtures/unused-names/src"]
                       ["fixtures/unused-names/test"]))))
