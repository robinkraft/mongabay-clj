(ns mongabay-clj.core
  (:use [cartodb.core]
        [clojure.data.json :only (read-json)])
  (:require [clojure.java.io :as io]
            [cheshire.custom :as json]
            [clj-http.client :as http]))

(def monga-bay-api-url "http://rfcx.org/mongabay")

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
    (-> (http/post monga-bay-api-url {:headers nil
                                      :save-request? true
                                      :debug-body true
                                      :body (body-encode
                                             location
                                             lat long
                                             title description
                                             thumbnail-url)})
        (:body)
        (json/parse-string))))

(defn- grab-feed
  "returns the json from server side"
  [])

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

(defn convert-keywords
  [m]
  (apply str m))

(defn convert-map
  "accepts "
  [m]
  (let [title (keys (first m))]
    (concat [title]
            (map vals m))))

;; {"1","10,11,12,13"}
