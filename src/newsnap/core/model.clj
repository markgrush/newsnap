(ns newsnap.core.model
  (:require [clojure.java.jdbc :as sql]
            [ring.util.response :as ring]))

; relative path of db url file
(def postgresql-url-file "postgresql_url.txt")

(defn get-postgres-url 
  []
  (-> (slurp postgresql-url-file)
    (clojure.string/split #" ")
    (second)))

(def spec (or (System/getenv "DATABASE_URL")
              (get-postgres-url)))

(defn migrated? 
  []
  (-> (sql/query spec
                 [(str "select count(*) from information_schema.tables "
                       "where table_name='news'")])
      first :count pos?))

(defn migrate 
  []
  (when (not (migrated?))
    (print "Creating database structure...") (flush)
    (sql/db-do-commands shout/spec
                        (sql/create-table-ddl
                         :news
                         [:id :serial "PRIMARY KEY"]
                         [:body :varchar "NOT NULL"]
                         [:created_at :timestamp
                          "NOT NULL" "DEFAULT CURRENT_TIMESTAMP"]))
    (println " done")))

(defn create
  [post]
  (when-not (clojure.string/blank? post)
    (sql/insert! spec :news [:body] post))
  (ring/redirect "/"))