(ns honey-ext.json
  "Implements all the JSON operators available in postgres.

  Docs here https://www.postgresql.org/docs/12/functions-json.html#FUNCTIONS-JSON-OP-TABLE."
  (:require [clojure.string :as string]
            [honey.sql :as sql]))

(defn- op->
  [_ path]
  (let [[sql-a & params] (sql/format-expr path)
        fmt-sql (-> sql-a
                    (string/replace #"\(" "->")
                    (string/replace #", " "->")
                    (string/replace #"\)" ""))]
    (-> [fmt-sql]
        (into params))))

(sql/register-fn! :-> op->)


(sql/format {:select [:*]
             :from [:honey]
             :where [:-> :data "key"]})
;; => ["SELECT * FROM honey WHERE DATA->?" "key"]

(sql/format {:select [:*]
             :from [:honey]
             :where [:-> :data :?key]}
            {:params {:key "key1"}})
;; => ["SELECT * FROM honey WHERE DATA->?" "key1"]

(sql/format {:select [:*]
             :from [:honey]
             :where [:-> :data :?key :?key2]}
            {:params {:key "key1"
                      :key2 "key2"}})
;; => ["SELECT * FROM honey WHERE DATA->?->?" "key1" "key2"]

;; better syntax for path:
(sql/format {:select [:*]
             :from [:honey]
             :where [:-> :data :?path]}
            {:params {:path ["key1" "key2"]}})
;; => ["SELECT * FROM honey WHERE DATA->?" ["key1" "key2"]]  <<<-- wrong
;; => ["SELECT * FROM honey WHERE DATA->?->?" "key1" "key2"]  <<<-- desired
