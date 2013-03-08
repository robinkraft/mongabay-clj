(ns mongabay-clj.core
  "This namespace retrieves articles from a Mongabay.com and uploads them to CartoDB. The
   articles are then displayed on a map at http://news.mongabay.com/json.

   Two credentials files are necessary for this to work:
     resources/creds.json
     resources/aws_creds.json"
  (:use [cartodb.playground]
        [mongabay-clj.sqlize]
        [clojure.data.json :only (read-json)]
        [clj-aws.core]
        [clj-aws.ses])
  (:require [cartodb.core :as carto]
            [clojure.java.io :as io]
            [cheshire.custom :as json]
            [clj-http.client :as http])
  (:gen-class :main true))

(def field-vec
  "vector of fields (as keywords) to be uploaded to cartodb"
  [:guid :loc :lat :lon :title :thumbnail :description :published]) ;; :keywords :author :updated

(def mongabay-url
  "a JSON endpoint for all mongabay articles with geo-coordinates"
  "http://news.mongabay.com/json/")

(defn get-creds
  [fname]
  (let [json-creds (io/resource fname)]
    (if (nil? json-creds)
      (throw (Exception. (format "%s must be in resources path" fname)))
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
  [table-name cartodb-creds]
  (let [data (convert-entries (mongabay-query))]
    (do

      ;; delete existing entries
      (delete-all "mongabay" cartodb-creds table-name)

      ;; insert the new stories as rows into the cartodb table
      (apply insert-rows "mongabay" cartodb-creds table-name data)

      ;; georeference the table using the coordinate variables named
      ;; lat and lon
      (carto/query
       (str "UPDATE " table-name " SET the_geom=cdb_latlng(lat,lon)")
       "mongabay" :oauth cartodb-creds))))

(defn notify-by-email
  ""
  [aws-creds address-vec body]
  (let [subject "Mongabay map update"
        clnt (client (credentials (:access-id aws-creds) (:private-key aws-creds)))
        from "mongabay@gmail.com"
        dst (destination address-vec)
        msg (message subject body)]
    (send-email clnt from dst msg)))

(defn -main
  "Main function uploads new stories to cartodb and (optionally) sends a
   notification email upon completion.

   Usage:
     > java -jar mongabay-clj-0.1.0-SNAPSHOT-standalone.jar monga_test email@email.com"  
  [table & email-addresses]
  (let [cartodb-creds (get-creds "creds.json")
        aws-creds (get-creds "aws_creds.json")
        return-map (upload-stories table cartodb-creds)
        body (format "Uploaded %d stories to CartoDB table '%s'" (:total_rows return-map) table)]
    (if (seq? email-addresses)
      (notify-by-email aws-creds email-addresses body))))
