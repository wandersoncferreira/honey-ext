(ns honey-ext.fixtures
  (:require [cheshire.core :as json]
            [next.jdbc.sql :as sql])
  (:import org.postgresql.util.PGobject))

(defn- ->jsonb
  [data]
  (doto (PGobject.)
    (.setType "jsonb")
    (.setValue (json/generate-string data))))

(def basic-entries
  [{:id "1" :data {:key "value"}}
   {:id "2" :data {:key1 {:key2 "value2"}}}
   {:id "3" :data {:key1 "value1"
                   :key2 "value2"}}])

(defn basic-table-entries
  [db]
  (let [entries (map #(update % :data ->jsonb) basic-entries)]
   (sql/insert-multi! db "honey"
                      [:id :data]
                      (map vals entries))))
