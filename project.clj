(defproject mongabay-clj "0.1.0-SNAPSHOT"
  :description "parse mongabay stories, upload them to cartodb"
  :url "mongabay.com"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :resources-path "resources"
  :repositories {"conjars" "http://conjars.org/repo/"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.clojure/math.numeric-tower "0.0.1"]
                 [org.clojure/data.json "0.2.1"]
                 [clj-http "0.4.3"]
                 [cheshire "4.0.0"]
                 [cartodb-clj "1.5.0"]
                 [org.clojure/clojure-contrib "1.2.0"]]
  :profiles {:dev {:dependencies [[org.apache.hadoop/hadoop-core "0.20.2-dev"]
                                  [midje-cascalog "0.4.0"]
                                  [incanter/incanter-charts "1.3.0"]]
                   :plugins [[lein-swank "1.4.4"]
                             [lein-midje "2.0.0-SNAPSHOT"]]}})
