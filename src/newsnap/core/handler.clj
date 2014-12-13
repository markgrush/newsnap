(ns newsnap.core.handler
  (:require [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.adapter.jetty :as jetty]
            [hiccup.page :refer [html5 include-css]]))

(defroutes app-routes
  (GET "/" [] "Hello World")
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (handler/site app-routes))

(defn -main []
  (let [port (Integer/parseInt (get (System/getenv) "PORT" "5000"))]
    (jetty/run-jetty app-routes {:port port})))
