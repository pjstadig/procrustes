(ns procrustes.test.code
  (:use [procrustes.code] :reload)
  (:use [clojure.test])
  (:use [clojure.java.io :only [input-stream]]))

(deftest test-read-code
  (is (= '[(ns procrustes.example
             (:use [clojure.contrib.io :only [to-byte-array]]))

           (defn foo [x]
             (to-byte-array))

           (def- bar (delay (println "Hello, World!")))]
         (read-code (.getBytes "(ns procrustes.example
             (:use [clojure.contrib.io :only [to-byte-array]]))

           (defn foo [x]
             (to-byte-array))

           (def- bar (delay (println \"Hello, World!\")))")))))
