(defproject mongabay-clj "0.2.0-SNAPSHOT"
  :description "parse mongabay stories, upload them to cartodb"
  :url "mongabay.com"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :resources-path "resources"
  :main mongabay-clj.core
  :repositories {"conjars" "http://conjars.org/repo/"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.clojure/math.numeric-tower "0.0.1"]
                 [org.clojure/data.json "0.2.4"]
                 [clj-http "0.4.3"]
                 [clojureql "1.0.4"]
                 [cartodb-clj "1.5.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [org.clojars.robinkraft/clj-aws "0.0.2-SNAPSHOT"]
                 [clj-time "0.6.0"]]
  :profiles {:dev {:plugins [[lein-swank "1.4.4"]
                             [lein-midje "2.0.0-SNAPSHOT"]]}})
