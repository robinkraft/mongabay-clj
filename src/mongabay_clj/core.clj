(ns mongabay-clj.core
  (:use [clojure.data.json :only (read-json)])
  (:require [clojure.java.io :as io]
            [cheshire.custom :as json]
            [clj-http.client :as http]
            [cartodb.core :as cartodb]))

(def mongabay-url "http://rfcx.org/mongabay")

(defn body-encode
  "JSON encode POST body"
  [location lat long title description thumbnail-url]
  (->> {"location" location
        "lat" lat
        "long" long
        "title" title
        "description" description
        "thumbnail-url" thumbnail-url}
       (json/generate-string)))

(defn monga-post-query
  [^String location lat long ^String title ^String description ^String thumbnail-url]
  "Post a monga-bay query to get monga-bay content and
   interpret results."
  (let []
    (-> (http/post mongabay-url {:headers nil
                                 :save-request? true
                                 :debug-body true
                                 :body (body-encode
                                        location
                                        lat long
                                        title description
                                        thumbnail-url)})
        (:body)
        (json/parse-string))))

(def creds (read-json (slurp (io/resource "creds.json"))))

(def example-data
  [{:guid 1
    :loc 2
    :lat 4
    :lon 3
    :title "test title"
    :description "a"
    :thumbnail "http://a.com"
    :published "2013-12-04T14:55:00Z"
    :updated "2013-12-04T14:55:00Z"
    :keywords ["a" "b" "c"]}
   {:guid 2
    :loc 2
    :lat 4
    :lon 6
    :title "tester 2"
    :description "b"
    :thumbnail "http://b.com"
    :published "2013-12-04T14:55:00Z"
    :updated "2013-12-04T14:55:00Z"
    :keywords ["a" "b" "c"]}])

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
  (->> (json/generate-string coll {:pretty true :escape-non-ascii true})
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




