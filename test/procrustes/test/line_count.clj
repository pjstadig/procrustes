(ns procrustes.test.line-count
  (:use [procrustes.line-count] :reload)
  (:use [clojure.test]))

(deftest test-line-counts
  (is (= {:file {:src {"src/line_count/core.clj" 1},
                 :test {"test/line_count/test/core.clj" 5}}}
         (line-counts ["fixtures/line-count/src/"]
                      ["fixtures/line-count/test"])))
  (is (= {:file {:src {"line_count/core.clj" 1}}}
         (line-counts ["fixtures/line-count/src/"]
                      [])))
  (is (= {:file {:test {"line_count/test/core.clj" 5}}}
         (line-counts []
                      ["fixtures/line-count/test"])))
  (is (= {}
         (line-counts []
                      []))))
