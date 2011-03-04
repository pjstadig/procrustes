(ns procrustes.test.code
  (:refer-clojure :exclude [ns-name])
  (:use [procrustes.code] :reload)
  (:use [clojure.test])
  (:use [clojure.java.io :only [input-stream reader]]))

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

(deftest test-ns-require-name
  (is (= 'bar (ns-require-name '[bar])))
  (is (= 'bar (ns-require-name '(bar))))
  (is (= 'quux (ns-require-name '[bar :as quux]))))

(deftest test-ns-requires
  (is (= '#{{clojure.contrib.graph clojure.contrib.graph}
            {mk clojure.contrib.mock}
            {clojure.contrib.import-static clojure.contrib.import-static}}
         (set (ns-requires '(ns procrustes.example
                              (:require [clojure.contrib.graph]
                                        clojure.contrib.import-static)
                              (:use [clojure.contrib.io :only [to-byte-array]])
                              (:require [clojure.contrib.mock :as mk]))))))
  (testing "preserves metadata"
    (let [requires (ns-requires
                (first (read-code (.getBytes "(ns foo.bar
                              (:require [clojure.contrib.graph]
                                        clojure.contrib.import-static)
                              (:require [clojure.contrib.mock :as mk])) "))))]
      (is (= {:line 2}
             (meta (nth requires 0))))
      (is (= {:line 2}
             (meta (nth requires 1))))
      (is (= {:line 4}
             (meta (nth requires 2)))))))

(deftest test-ns-uses
  (is (= '#{{clojure.contrib.io [to-byte-array copy]}
            {clojure.contrib.condition [raise]}}
         (set (ns-uses '(ns procrustes.example
                          (:require [clojure.contrib.graph])
                          (:use [clojure.contrib.io :only [to-byte-array
                                                           copy]])
                          (:use [clojure.contrib.import-static])
                          (:use [clojure.contrib.condition :only [raise]])
                          (:require [clojure.contrib.mock :as mk]))))))
  (testing "preserves metadata"
    (let [uses (ns-uses
                (first (read-code (.getBytes "(ns foo.bar
                                 (:use [clojure.contrib.io :only [file]])
                                 (:use [foo.bar :only [baz]]
                                       [baz.foo :only [quux]]))"))))]
      (is (= {:line 2}
             (meta (nth uses 0))))
      (is (= {:line 3}
             (meta (nth uses 1))))
      (is (= {:line 3}
             (meta (nth uses 2)))))))

(deftest test-blocks
  (let [code (.getBytes "(defn foo [bar] (println bar))

                         (ns foo.bar
                           (:use [clojure.contrib :only [file]]))

                         (defn baz [x]
                           x)

                         (ns baz)
                         (def PI 3.14)")]
    (is (= '[[(ns user)
              (defn foo [bar] (println bar))]
             [(ns foo.bar
                (:use [clojure.contrib :only [file]]))

              (defn baz [x]
                x)]
             [(ns baz)
              (def PI 3.14)]]
          (blocks (read-code code))))))

(deftest test-search-code
  (let [code (.getBytes "(defn foo [bar] (println bar))

                         (ns foo.bar
                           (:use [clojure.contrib :only [file]]
                                 [bar.baz]))

                         (defn baz [x]
                           x)

                         (ns baz)
                         (def PI 3.14)")
        use-form (first (search-code (partial application? :use)
                                     (read-code code)))]
    (is (= {:line 4}
           (meta (first (search-code (fn [e]
                                       (and (vector? e)
                                            (= (first e) 'bar.baz)))
                                     use-form)))))))
