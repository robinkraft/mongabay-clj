(ns mongabay-clj.core
  (:use [cartodb.playground]
        [mongabay-clj.sqlize]
        [clojure.data.json :only (read-json)])
  (:require [cartodb.core :as carto]
            [clojure.java.io :as io]
            [cheshire.custom :as json]
            [clj-http.client :as http]))

(def field-vec
  "vector of fields (as keywords) to be uploaded to cartodb"
  [:guid :loc :lat :lon :title :thumbnail :description])

(def mongabay-url
  "a JSON endpoint for all mongabay articles with geo-coordinates"
  "http://news.mongabay.com/map/json/")

(def creds
  "cartodb credentials stored as a JSON object in the resources
  directory"
  (let [json-creds (io/resource "creds.json")]
    (if (nil? json-creds)
      (throw (Exception. "creds.json must be in resources path"))
      (read-json (slurp json-creds)))))

(defn mongabay-query
  "Fetch and decode mongabay JSON"
  []
  (-> (http/get mongabay-url)
      (:body)
      (json/parse-string true)))

(defn doto-map
  "accepts a map, vector of keys, and a function (with arguments) to
  apply the parameterized function to the specified key-values."
  [m ks f & args]
  (reduce #(apply update-in %1 [%2] f args) m ks))

(defn prep-entry
  "accepts a map and returns a restricted and cleaned version for
  translation into the SQL query"
  [m]
  (-> (select-keys m field-vec)
      (doto-map [:title :description] clean-str)))

(defn convert-entries
  "accepts a collection of maps and converts them into the appropriate
  clojure data structures."
  [coll]
  (let [sub-coll (map prep-entry coll)]
    (concat [(-> sub-coll first keys vec)]
            (map (comp vec vals) sub-coll))))

(defn upload-stories
  "grabs the JSON of the stories with the supplied keys, translates
  the resulting string from the post request, and then uploads the
  specified fields to our cartodb table; will overwrite existing table
  and then georeference the latitude and longitude fields.

  Example usage:
    (upload-stories \"monga_test\")
    => {:time 0.061, :total_rows 331, :rows []}"
  [table-name]
  (let [data (convert-entries (mongabay-query))]
    (do

      ;; delete existing entries
      (delete-all "mongabay" creds table-name)

      ;; insert the new stories as rows into the cartodb table
      (apply insert-rows "mongabay" creds table-name data)

      ;; georeference the table using the coordinate variables named
      ;; lat and lon
      (carto/query
       (str "UPDATE " table-name " SET the_geom=cdb_latlng(lat,lon)")
       "mongabay" :oauth creds))))

