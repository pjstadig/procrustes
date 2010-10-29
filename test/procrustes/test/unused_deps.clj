(ns procrustes.test.unused-deps
  (:use [procrustes.unused-deps] :reload)
  (:use [clojure.test]
        [procrustes.code :only [read-code]]))

(deftest test-unused-deps
  (is (= '{procrustes.example [clojure.java.io/file
                               clojure.contrib.monads]}
         (unused-deps [(.getBytes "(ns procrustes.example
             (:require [clojure.contrib.monads :as monad]
                       [clojure.contrib.ns-utils :as utils])
             (:use [clojure.contrib.io :only [to-byte-array]]
                   [clojure.java.io :only [file]])
             (:use [clojure.contrib.mock]))

           (defn foo [x]
             (utils/docs procrustes.example)
             (to-byte-array))

           (def- bar (delay (println \"Hello, World!\")))")]))))
