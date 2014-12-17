(ns newsnap.core.schema
  (:require [clojure.java.jdbc :as sql]
            [newsnap.core.model :as model]))

(defn migrated? 
  []
  (-> (sql/query model/spec
                 [(str "select count(*) from information_schema.tables "
                       "where table_name='news'")])
      first :count pos?))

(defn migrate 
  []
  (when (not (migrated?))
    (print "Creating database structure...") (flush)
    (sql/db-do-commands model/spec
                        (sql/create-table-ddl
                         :news
                         [:id :serial "PRIMARY KEY"]
                         [:countdownkey :varchar "NOT NULL"]
                         [:name :varchar "NOT NULL"]
                         [:email :varchar "NOT NULL"]
                         [:title :varchar "NOT NULL"]
                         [:body :varchar "NOT NULL"]
                         [:created_at :timestamp
                          "NOT NULL" "DEFAULT CURRENT_TIMESTAMP"]))
    (println " done")))