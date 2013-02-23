(ns mongabay-clj.core
  (:use [cartodb.playground :only (big-insert insert-rows)]
        [clojure.data.json :only (read-json)]
        [clojure.contrib.string :only (chop)])
  (:require [clojure.java.io :as io]
            [cheshire.custom :as json]
            [clj-http.client :as http]))

(def mongabay-url "http://rfcx.org/mongabay")

(def creds (read-json (slurp (io/resource "creds.json"))))

(defn monga-get-query
  "Fetch and decode monga query"
  []
  (-> (http/get mongabay-url)
      (:body)
      (json/parse-string true)))

(defn surround-str
  "Surround a supplied string with supplied string."
  [s surround-with]
  (format "%s%s%s" surround-with s surround-with))

(defn concat-results
  "Concatenate a collection of strings, with an optional separator."
  [results-vec & [sep]]
  (apply str (interpose sep results-vec)))

(defn prep-vals
  "Format collection for insert, including adding quotes and {}."
  [coll]
  (->> (json/generate-string coll {:pretty true})
       (drop 1)
       (butlast)
       (apply str)
       (format "{%s}")
       (#(surround-str % "'"))))

(defn doto-map
  "accepts a map, vector of keys, and a function (with arguments) to
  apply the parameterized function to the specified key-values."
  [m ks f & args]
  (reduce #(apply update-in %1 [%2] f args) m ks))

(defn convert-entries
  "accepts a collection of maps and converts them into the appropriate
  clojure data structures."
  [coll]
  (let [updated-map (map #(doto-map % [:keywords] prep-vals) coll)]
    (concat [(keys (first coll))]
            (map vals updated-map))))

(defn try-me []
  (let [data (convert-entries (monga-get-query))]
    (apply insert-rows "mongabay" creds "mongabaydb" data)))



