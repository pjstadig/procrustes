(ns unused-names.core
  (:require [clojure.contrib.combinatorics]
            [clojure.contrib.str-utils :as stru] :reload-all)
  (:use [clojure.contrib.jar :only [jar-file?]]
        [clojure.contrib.graph :only [add-loops]])
  (:require [clojure.contrib.io]
            [clojure.contrib.command-line :as cl] :reload)
  (:use [clojure.contrib.def]))

(defn foo [s]
  (clojure.contrib.io/file (stru/chop s)))

(defn bar [j]
  (jar-file? j))
