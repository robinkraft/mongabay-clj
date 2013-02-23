(ns mongabay-clj.core
  (:use [cartodb.core]
        [clojure.data.json :only (read-json)]
        [clojure.java.io :as io]))

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
