(ns clj-date.test.core
  (:require [clj-date.core :refer [date month-step]]
            [clojure.test :refer :all]))


(deftest month-step-test
  (are [d n d'] (= (month-step (date d) n) (date d'))
       [2012 7 5]    2 [2012 9 5]
       [2012 7 5]    0 [2012 7 5]
       [2012 7 5]   -3 [2012 4 5]
       [2012 11 1]   5 [2013 4 1]
       [2012 11 2] -17 [2011 6 2]))