(ns audio-test
  (:require [clojure.test :refer [deftest is testing]]
            [audio]))
(deftest namespace-loads
  (testing "the restored CLJC namespace loads"
    (is (some? audio))))
