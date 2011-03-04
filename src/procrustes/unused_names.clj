(ns procrustes.unused-names
  (:use [procrustes.code :only [search-code ns? ns-requires ns-uses
                                read-code-from-file blocks]]
        [procrustes.file :only [find-files canonicalize-file]]
        [clojure.java.io :only [file]]
        [clojure.set :only [union]]))

(defn namespace? [name e]
  (and (or (symbol? e)
           (keyword? e))
       (= (namespace e) (str name))))

(defn unused-required-names [ns requires body]
  (->> requires
       (filter (fn [r]
                 (let [[name r] (first r)]
                   (empty? (search-code (partial namespace? name)
                                        body)))))
       (map (fn [r]
              (let [[name r] (first r)]
                name)))
       set))

(defn unused-used-names [ns uses body]
  (apply concat
         (for [use uses]
           (let [[ns names] (first use)]
             (->> names
                  (filter (fn [n]
                            (empty? (search-code (partial = n) body))))
                  (map (fn [n] (symbol (str ns) (str n))))
                  set)))))

(defn unused-names-in-ns [[ns & body]]
  (let [body (if (not (ns? ns))
               (cons ns body)
               body)
        unused-required-names (unused-required-names (second ns)
                                                     (ns-requires ns)
                                                     body)
        unused-used-names (unused-used-names (second ns)
                                             (ns-uses ns)
                                             body)]
    (union unused-required-names unused-used-names)))

(defn unused-names-in-file [base file]
  (let [unused-names (reduce union (map unused-names-in-ns
                                        (blocks (read-code-from-file file))))]
    (if (empty? unused-names)
      {}
      {:file {(canonicalize-file file base)
              unused-names}})))

(defn unused-names
  ([src-dirs test-dirs]
     (apply unused-names (find-files src-dirs test-dirs)))
  ([base src-files test-files]
     (let [src-names (->> src-files
                          (map (partial unused-names-in-file base))
                          (reduce (partial merge-with merge) {}))
           test-names (->> test-files
                           (map (partial unused-names-in-file base))
                           (reduce (partial merge-with merge) {}))]
       (merge-with merge src-names test-names))))
