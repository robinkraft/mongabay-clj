(ns mongabay-clj.core
  (:use [cartodb.playground]
        [cartodb.core]
        [clojure.data.json :only (read-json)]
        [clojure.contrib.string :only (chop)])
  (:require [clojure.java.io :as io]
            [cheshire.custom :as json]
            [clj-http.client :as http]))

(def field-vec
  "vector of fields (as keywords) to be uploaded to cartodb"
  [:loc :lat :lon :title :thumbnail :description])

(def mongabay-url
  "a JSON endpoint for all mongabay articles with geo-coordinates"
  "http://news.mongabay.com/json/")

(def creds
  "cartodb credentials stored as a JSON object in the resources
  directory"
  (read-json (slurp (io/resource "creds.json"))))

(defn mongabay-query
  "Fetch and decode mongabay JSON"
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

(defn- crop-string
  "accepts a string and crops the first and last characters, returns
  the original but middle string"
  [s]
  (->> s (drop 1) (butlast) (apply str)))

(defn prep-vals
  "Format collection for insert, including adding quotes and {} " [coll]
  (->> (json/generate-string coll {:pretty true})
       (crop-string)
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

(defn clean-str
  "accepts a string and preps it for an SQL query, specifically
  removing any single quotes"
  [s]
  (.replaceAll s "[']" ""))

(defn clean-collection
  "Remove single apostrophes from the title and description values in
  the supplied collection of hash maps"
  [coll]
  (map #(doto-map % [:title :description] clean-str) coll))

(defn add-field
  "accepts a hash map and a new description value, wrapped in a
  vector, and replaces the old description value"
  [m [new-desc]]
  (assoc m :description new-desc))

(defn convert-this
  "accepts a collection of maps and converts them into the appropriate
  clojure data structures."
  [coll]
  (let [desc (map (comp vector str :description) (clean-collection coll))
        up-coll (map add-field coll desc)
        new-coll (map #(select-keys % field-vec) up-coll)
        title (vec (keys (first new-coll)))]
    (concat [title]
            (map (comp vec vals) new-coll))))

(defn upload-stories
  "grabs the JSON of the stories with the supplied keys, translates
  the resulting string from the post request, and then uploads the
  specified fields to our cartodb table; will overwrite existing table
  and then georeference the latitude and longitude fields"
  []
  (let [data (convert-this (mongabay-query))]
    (do (delete-all "mongabay" creds "mongabaydb")
        (apply insert-rows "mongabay" creds "mongabaydb" data)
        (query "UPDATE mongabaydb SET the_geom=cdb_latlng(lat,lon)" "mongabay"
               :oauth creds))))
