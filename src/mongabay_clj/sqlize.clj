(ns mongabay-clj.sqlize
  (:use [clojureql.internal]
        [clojure.contrib.string :only (chop)])
  (:require [cheshire.custom :as json]))

(defn surround-str
  "Surround a supplied string with supplied string."
  [s surround-with]
  (format "%s%s%s" surround-with s surround-with))

(defn concat-results
  "Concatenate a collection of strings, with an optional separator."
  [results-vec & [sep]]
  (apply str (interpose sep results-vec)))

(defn- crop-string
  "accepts a string and crops the first and last characters, returns
  the original but middle string"
  [s]
  (->> s (drop 1) (butlast) (apply str)))

(defn prep-vals
  "Format collection for insert, including adding quotes and {} "
  [coll]
  (->> (json/generate-string coll {:pretty true})
       (crop-string)
       (format "{%s}")
       (#(surround-str % "'"))))

(defn clean-str
  "accepts a string and preps it for an SQL query, specifically
  removing any single quotes"
  [s]
  (.replaceAll s "[']" ""))
