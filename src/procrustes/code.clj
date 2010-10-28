(ns procrustes.code
  (:use [clojure.java.io :only [reader]])
  (:import [java.io PushbackReader]))

(defn- read-code* [s code]
  (let [e (read s false ::eof)]
    (if (= ::eof e)
      code
      (recur s (conj code e)))))

(defn read-code [s]
  (read-code* (PushbackReader. (reader s)) []))
