(ns honey-ext.db
  (:require [cheshire.core :as json]
            [honey-ext.fixtures :as fixtures]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]
            [next.jdbc.sql :as sql]
            [clojure.walk :as walk])
  (:import io.zonky.test.db.postgres.embedded.EmbeddedPostgres
           org.postgresql.util.PGobject))

(def ^:dynamic *db* nil)

(defn- get-new-connection
  []
  (let [pg (EmbeddedPostgres/start)]
    (-> pg
        (.getPostgresDatabase)
        (.getConnection))))

(def ^:private create-table-stmt
  "create table honey (id varchar(1024) NOT NULL PRIMARY KEY, data jsonb NOT NULL)")

(defn- init-db!
  []
  (jdbc/execute! *db* [create-table-stmt])
  (fixtures/basic-table-entries *db*))

(defn with-pg-fn
  [f]
  (try
    (binding [*db* (get-new-connection)]
      (init-db!)
      (f))
    (finally)))

(defn <-jsonb
  [data]
  (json/parse-string (.getValue data) true))

(defn query
  [stmt]
  (let [res (sql/query *db* stmt {:builder-fn rs/as-unqualified-kebab-maps})]
    (walk/postwalk (fn [entry]
                     (if (instance? PGobject entry)
                       (<-jsonb entry)
                       entry))
                   res)))
