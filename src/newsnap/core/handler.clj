(ns newsnap.core.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.util.response :as ring]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults api-defaults]]
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
                             :value-fn timestamp-to-string)
        {:message "You requested a media type"
         :media-type content-type}))))
      
(defroutes app-routes
  (GET "/" [] all-threads-resource)
  (POST "/" [op-name op-email title news] 
        (model/create op-name op-email title news)
        (ring/redirect "/"))
  ;; next time MAKE SURE the :id thingy has a regular expression with it 
  ;; what happened was that it was just :id and the server loads the css file as
  ;; /cssfile.css and triggers this get which can cause problems.
  (GET "/:id{n[0-9]+}" [id] (thread-resource id))
  (POST "/:id{n[0-9]+}" [id replier-name replier-email reply] 
        (model/create-reply id replier-name replier-email reply)
        (ring/redirect (str "/" id)))
  (route/resources "/"))

;; routes for mobile - wrapped without browser specific middleware
(defroutes mobile-routes
  (POST "/mobile" [op-name op-email title news] 
        (model/create op-name op-email title news)
        (ring/response "OK"))
  (POST "/mobile/:id{n[0-9]+}" [id replier-name replier-email reply] 
        (model/create-reply id replier-name replier-email reply)
        (ring/response "OK")))

;; when combining multiple routes, we must make sure the "route/not-found"
;; is in a separate route and placed at the end of all other routes
;; when combining them below.
(defroutes not-found-route
  (route/not-found "Not Found"))

(def app
  ;; since compojure.handler wrappers are deprecated, we're using 
  ;; ring-defaults wrappers instead.
  ;; we set the proxy to true. according to ring-defaults doc, 
  ;; when an "app is sitting behind a load balancer or reverse proxy, 
  ;; as is often the case in cloud-based deployments" we should set
  ;; the proxy (in the middleware map) to true.
  ;; we also use "wrap-routes" so that the middleware will be applied
  ;; AFTER the urls are matched. we do that because the api-defaults middleware
  ;; is applied even to requests of the other routes with the different
  ;; middleware, so we use that function to prevent it.
  ;; NON SECURE solution below, at least for now until I figure out 
  ;; what to do with the middleware interleaving...
    (wrap-defaults
      (routes mobile-routes app-routes not-found-route)
      (assoc api-defaults :proxy true)))

(defn start [port]
  (jetty/run-jetty app {:port port :join? false}))

(defn -main []
  (schema/migrate)
  (let [port (Integer. (or (System/getenv "PORT") "5000"))]
    (start port)))
