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
  (into [] (sql/query spec ["select title from news order by id desc"])))

(defn create
  [name email title news]
  (when (and 
          (not (clojure.string/blank? title))
          (not (clojure.string/blank? news)))
    (sql/insert! spec :news {:name name :email email :title title :body news}))
  (ring/redirect "/"))