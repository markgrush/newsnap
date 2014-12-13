(ns newsnap.core.handler
  (:require [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.adapter.jetty :as jetty]
            [hiccup.page :refer [html5 include-css]]))

(def title "Newsnap")

(defn root [& body]
  (html5
    [:html
     [:head
      (include-css "simple.css")
      [:title title]]
     [:body body]))
     
(def test
  [:div {:class "post"} 
   [:p "Breaking news: Botnim!"]])
      

(defroutes app-routes
  (GET "/" [] (root test))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (handler/site app-routes))

(defn -main []
  (let [port (Integer/parseInt (get (System/getenv) "PORT" "5000"))]
    (jetty/run-jetty app-routes {:port port})))
