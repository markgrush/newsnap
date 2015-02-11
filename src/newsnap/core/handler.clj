(ns newsnap.core.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [hiccup.form :as form]
            [hiccup.page :refer [html5 include-css]]
            [hiccup.util :refer [escape-html]]
            [newsnap.core.model :as model]
            [newsnap.core.schema :as schema]
            [liberator.core :refer [defresource]]
            [clojure.data.json :as json]
            
            [newsnap.view.pages :refer [main-page thread-page]])
  (:gen-class))

;; convert java.sql.Timestamp to string. Happens only if key is createdAt
;; which should have a timestamp value from database.
(defn timestamp-to-string [key val]
  (if (= key :createdAt)
    (.toString val)
    val))

(defresource all-threads-resource
  :available-media-types
  ["text/html" "application/json"]
  :handle-ok
  (fn [ctx]
    (let [content-type (get-in ctx [:representation :media-type])]
      (condp = content-type
        "text/html" main-page
        "application/json" (json/write-str
                             (into
                               []
                               (map (fn [query] 
                                      {:threadId (:countdownkey query)
                                       :createdAt (:created_at query)
                                       :title (:title query)
                                       :name (:name query)
                                       :email (:email query)})
                                    (model/all-news)))
                             ;; value-fn : takes function that accepts
                             ;; key and value and returns processed value.
                             ;; used to convert complex data structures
                             ;; to ones that can be jsonified. In this case
                             ;; we convert java.sql.Timestamp to plain string.
                             :value-fn timestamp-to-string)
        {:message "You requested a media type"
         :media-type content-type}))))

(defresource thread-resource
  [thread]
  :available-media-types 
  ["text/html" "application/json"]
  :handle-ok 
  (fn [ctx] 
    (let [content-type (get-in ctx [:representation :media-type])]
      (condp = content-type
        "text/html" (thread-page thread)
        ;; this is awkward.. but good enough for now
        "application/json" (json/write-str 
                             (into 
                               [] 
                               (map (fn [query] 
                                      {:title (:title query)
                                       :createdAt (:created_at query)
                                       :name (:name query)
                                       :email (:email query)
                                       :body (:body query)})
                                 (model/news-item thread)))
                             :value-fn timestamp-to-string))
        {:message "You requested a media type"
         :media-type content-type}))))
      
(defroutes app-routes
  (GET "/" [] all-threads-resource)
  (POST "/" [op-name op-email title news] (model/create op-name op-email title news))
  ;; next time MAKE SURE the :id thingy has a regular expression with it 
  ;; what happened was that it was just :id and the server loads the css file as
  ;; /cssfile.css and triggers this get which can cause problems.
  (GET "/:id{n[0-9]+}" [id] (thread-resource id))
  (POST "/:id{n[0-9]+}" [id replier-name replier-email reply] (model/create-reply id replier-name replier-email reply))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  ;; since compojure.handler wrappers are deprecated, we're using 
  ;; ring-defaults wrappers instead.
  (-> app-routes
    ;; this part is important (according to ring-defaults doc) 
    ;; when "app is sitting behind a load balancer or reverse proxy, 
    ;; as is often the case in cloud-based deployments"
    (wrap-defaults (assoc site-defaults :proxy true))))

(defn start [port]
  (jetty/run-jetty app {:port port :join? false}))

(defn -main []
  (schema/migrate)
  (let [port (Integer. (or (System/getenv "PORT") "5000"))]
    (start port)))
