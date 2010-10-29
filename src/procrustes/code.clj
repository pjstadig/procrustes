(ns procrustes.code
  (:use [clojure.java.io :only [reader]])
  (:import [java.io PushbackReader]))

(defn- search-code* [code pred matches]
  (if (pred code)
    (conj matches code)
    (if (and (coll? code)
             (not (empty? code)))
      (mapcat #(search-code* % pred matches) code))))

(defn search-code [code pred]
  (search-code* code pred []))

(defn find-ns-form [code]
  (first (for [x code :when (= 'ns (first x))]
           x)))

(defn find-ns-name [code]
  (second (find-ns-form code)))

(defn but-ns-form [code]
  (for [x code :when (not= 'ns (first x))]
    x))

(defn- read-code* [s code]
  (let [e (read s false ::eof)]
    (if (= ::eof e)
      code
      (recur s (conj code e)))))

(defn read-code [s]
  (read-code* (PushbackReader. (reader s)) []))
