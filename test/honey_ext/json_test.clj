(ns honey-ext.json-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [honey-ext.db :as db]
            [honey-ext.json]
            [honey-ext.fixtures :as fixtures]
            [honey.sql :as sql]))

(use-fixtures :once db/with-pg-fn)

(deftest simple-test
  (testing "can I read the fixture data?"
    (is (= (db/query "select * from honey")
           fixtures/basic-entries))))

(deftest ->operator-test
  (testing "Get JSON array element (indexed from zero, negative integers count from the end)"
    (let [sql-output (sql/format {:select [[[:-> :data "key1"] :valor]]
                                  :from [:honey]})
          sql-inline-output (sql/format {:select [[[:-> :data [:inline "key1"]] :valor]]
                                         :from [:honey]})
          expected-output [{:valor nil}
                           {:valor {:key2 "value2"}}
                           {:valor "value1"}]]

      (is (= ["SELECT DATA->? AS valor FROM honey" "key1"] sql-output))
      (is (= expected-output (db/query sql-output)))
      (is (= ["SELECT DATA->'key1' AS valor FROM honey"] sql-inline-output))
      (is (= expected-output (db/query sql-inline-output))))

    (testing "multiple parameters"
      (let [sql-output (sql/format {:select [[[:-> :data "key1" "key2"] :valor]]
                                    :from [:honey]})
            expected-output [{:valor nil}
                             {:valor "value2"}
                             {:valor nil}]]
        (is (= ["SELECT DATA->?->? AS valor FROM honey" "key1" "key2"]
               sql-output))
        (is (= expected-output (db/query sql-output)))))))
