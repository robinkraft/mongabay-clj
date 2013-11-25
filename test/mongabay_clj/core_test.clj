(ns mongabay-clj.core-test
  (:use mongabay-clj.core
        [midje sweet]))

(future-fact
  (let [return-map (upload-stories "monga_test")]
    (>  (:total_rows return-map) 0)) => true)

(fact "Check `date-ify`"
  (date-ify "December 25, 1999") => "1999-12-25"
  (date-ify "12 25, 1999") => (throws IllegalArgumentException))
