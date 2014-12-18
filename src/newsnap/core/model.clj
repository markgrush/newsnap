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

(defn news-item
  [id]
  (sql/query spec [(str "select * from " id " order by id asc")]))

;special key to identify the news report when counting down its deletion timer
(defn countdown-key
  []
  (str "n" (int (rand 90000000))))

(defn countdown [title news key]
  (Thread/sleep 6000)
  (sql/query spec [(str "delete from news where title='" title "' and body='" news "' and countdownkey=" key)])
  (sql/query spec [(str "drop table " key)]))


(defn exists?
  "Check whether a given table exists."
  [db-spec table]
  (try
    (do
      (->> (str "select 1 from " table)
        (vector)
        (sql/query db-spec))
      true)
    (catch Throwable ex
      false)))

(defn create-table [table-name]
  "creates table with original news story and replies"
  [table-name]
  (sql/db-do-commands spec (sql/create-table-ddl (keyword table-name)
                                            [:id :serial "PRIMARY KEY"]
                                            [:name :varchar "NOT NULL"]
                                            [:email :varchar "NOT NULL"]
                                            [:title :varchar "NOT NULL"]
                                            [:body :varchar "NOT NULL"]
                                            [:created_at :timestamp
                                             "NOT NULL" "DEFAULT CURRENT_TIMESTAMP"])))

(defn create
  [name email title news]
  (when (and 
          (not (clojure.string/blank? title))
          (not (clojure.string/blank? news)))
    (let [key (countdown-key)]
      (sql/insert! spec :news {:name name :email email :title title :body news :countdownkey key})
      (when-not (exists? spec key)
        (create-table key))
      (sql/insert! spec (keyword key) {:name name :email email :title title :body news})
      (future (countdown title news key))))
  (ring/redirect "/"))

(defn keywordize
  [string]
  (if (= (first "/") (first string))
    (keyword (subs string 1))
    (keyword string)))

(defn create-reply
  [table name email reply]
  (when-not (clojure.string/blank? reply)
    (sql/insert! spec (keywordize table) {:name name :email email :title "" :body reply}))
  (ring/redirect (str "/" table)))