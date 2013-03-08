(ns mongabay-clj.core-test
  (:use mongabay-clj.core
        [midje sweet]))

(fact
  (let [return-map (upload-stories "monga_test")]
    (>  (:total_rows return-map) 0)) => true)
