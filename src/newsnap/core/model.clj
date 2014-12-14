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

(defn all-news
  []
  (into [] (sql/query spec ["select * from news order by id desc"])))

(defn create
  [post]
  (when-not (clojure.string/blank? post)
    (sql/query spec [(str "insert into news (body) values (" post ")")])
 ;   (sql/insert! spec :news {:body post}))
  (ring/redirect "/"))