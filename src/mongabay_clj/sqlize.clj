(ns mongabay-clj.sqlize
  (:use [clojureql.internal]
        [clojure.contrib.string :only (chop)]))

(defn clean-str
  "accepts a string and preps it for an SQL query, specifically
  removing any single quotes"
  [s]
  (.replaceAll s "[']" "''"))

(defn csv-ify
  [coll]
  {:pre [(every? string? coll)]}
  (clojure.string/join "," coll))
