(ns procrustes.code
  (:use [clojure.java.io :only [reader file]])
  (:import (clojure.lang LineNumberingPushbackReader
                         LispReader
                         LispReader$ReaderException
                         IMeta)))

(defn- map-with-meta
  ([seq]
     (map-with-meta identity seq))
  ([f seq]
     (map (fn [x]
            (let [y (f x)]
              (if (and (instance? IMeta y)
                       (empty? (meta y))
                       (or (meta x)
                           (meta seq)))
                (with-meta y (or (meta x)
                                 (meta seq)))
                y)))
          seq)))

(defn- mapcat-with-meta
  ([coll]
     (mapcat-with-meta identity coll))
  ([f coll]
     (reduce concat
             (map-with-meta (fn [x]
                              (let [x (if (sequential? x)
                                        (map-with-meta x)
                                        x)
                                    y (f x)]
                                (if (meta y)
                                  y
                                  (with-meta y (meta x))))) coll))))

(defn- rest-with-meta [s]
  (let [r (rest s)]
    (if (meta r)
      r
      (with-meta r (meta s)))))

(defn search-code
  ([pred code]
     (search-code pred code []))
  ([pred code matches]
     (if (pred code)
       (conj matches code)
       (if (and (coll? code)
                (not (empty? code)))
         (mapcat #(search-code pred % matches)
                 (map-with-meta code))))))

(defn application? [name form]
  (and (list? form) (= name (first form))))

(defn ns? [form]
  (application? 'ns form))

(defn ns-require-name
  ([form]
     (ns-require-name form (first form)))
  ([form name]
     (if (seq form)
       (if (= :as (first form))
         (fnext form)
         (recur (rest form) name))
       name)))

(defn ns-requires [form]
  {:pre [(ns? form)]}
  (->> (filter (partial application? :require) form)
       (mapcat-with-meta rest-with-meta)
       (remove #{:reload :reload-all})
       (map-with-meta (fn [r]
                        (if (sequential? r)
                          {(ns-require-name r) (first r)}
                          {r r})))))

(defn ns-use-onlys [form]
  (if (and (sequential? form)
           (seq form))
    (if (= :only (first form))
      (fnext form)
      (recur (rest form)))))

(defn ns-uses [form]
  {:pre [(ns? form)]}
  (->> (filter (partial application? :use) form)
       (mapcat-with-meta rest-with-meta)
       (map-with-meta (fn [u]
                        (if-let [onlys (ns-use-onlys u)]
                          {(first u) onlys})))
       (filter identity)))

(defn blocks
  ([code]
     (if (not (ns? (first code)))
       (blocks code '[(ns user)] [])
       (blocks code [] [])))
  ([code block blocks]
     (if (seq code)
       (if (ns? (first code))
         (recur (rest code) [(first code)] (if (seq block)
                                             (conj blocks block)
                                             blocks))
         (recur (rest code) (conj block (first code)) blocks))
       (if (seq block)
         (conj blocks block)
         blocks))))

(defn read-code
  ([s]
     (read-code (LineNumberingPushbackReader. (reader s)) []))
  ([s code]
     (let [e (LispReader/read s false ::eof false)]
       (if (= ::eof e)
         code
         (recur s (conj code e))))))

(defn unwrap-runtime-exception [e]
  (if (and (instance? RuntimeException e)
           (.getCause e))
    (recur (.getCause e))
    e))

(defn read-code-from-file [f]
  (try
    (read-code (file f))
    (catch LispReader$ReaderException e
      (throw (RuntimeException. (str "Error reading " f) e)))
    (catch RuntimeException e
      (let [c (unwrap-runtime-exception e)]
        (if (instance? LispReader$ReaderException c)
          (throw (RuntimeException. (str "Error reading " f) c))
          (throw e))))))
