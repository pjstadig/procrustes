(ns procrustes.unused-deps
  (:use [procrustes.code :only [search-code find-ns-form but-ns-form
                                find-ns-name read-code]]))

(defn- use? [code]
  (and (list? code) (= :use (first code))))

(defn- find-uses [code]
  (mapcat rest (search-code (find-ns-form code) use?)))

(defn- find-use-onlys [code]
  (reduce merge {}
          (map (fn [[ k _ v]] (hash-map k v))
               (filter #(= :only (second %)) (find-uses code)))))

(defn- used? [code sym]
  (not (empty? (search-code code (partial = sym)))))

(defn- remove-used-use-only-syms [code uses]
  (reduce (fn [m [ns syms]]
            (merge m {ns (remove (partial used? code) syms)}))
          {}
          uses))

(defn- unused-uses [code]
  (let [but-ns-form (but-ns-form code)]
    (->> code
         (find-use-onlys)
         (remove-used-use-only-syms but-ns-form)
         (remove (fn [[ns syms]]
                   (empty? syms)))
         (mapcat (fn [[ns syms]]
                   (map (fn [sym]
                          (symbol (str ns "/" sym)))
                        syms))))))

(defn- require? [code]
  (and (list? code) (= :require (first code))))

(defn- find-requires [code]
  (mapcat rest (search-code (find-ns-form code) require?)))

(defn- find-require-ases [code]
  (reduce merge {}
          (map (fn [[ k _ v]] (hash-map k v))
               (filter #(= :as (second %)) (find-requires code)))))

(defn- require-as-used? [code name]
  (not (empty? (search-code code (fn [code]
                                   (and (symbol? code)
                                        (.startsWith (str code)
                                                     (str name "/"))))))))

(defn- remove-used-require-ases [code requires]
  (remove (fn [[ns name]]
            (require-as-used? code name))
          requires))

(defn- unused-requires [code]
  (let [but-ns-form (but-ns-form code)]
    (->> code
         (find-require-ases)
         (remove-used-require-ases but-ns-form)
         (map first))))

(defn unused-deps [files]
  (->> files
       (map read-code)
       (map (fn [code]
              {(find-ns-name code)
               (concat (unused-uses code)
                       (unused-requires code))}))
       (reduce merge {})))
