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

;special key to identify the news report when counting down its deletion timer
(defn countdown-key
  []
  (int (rand 90000000)))

(defn countdown [title news key]
  (Thread/sleep 10000)
  ;TODO: delete also from the table with the actual news post and replies
  (sql/query spec [(str "delete from news where title='" title "' and body='" news "' and countdownkey=" key ";")]))

(defn create
  [name email title news]
  (when (and 
          (not (clojure.string/blank? title))
          (not (clojure.string/blank? news)))
    (let [key (countdown-key)]
      (sql/insert! spec :news {:name name :email email :title title :body news :countdownkey key})
      (.start (Thread. (countdown title news key)))))
  (ring/redirect "/"))