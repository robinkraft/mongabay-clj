(ns mongabay-clj.core
  (:use [cartodb.core])
  (:require [cheshire.custom :as json]
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
