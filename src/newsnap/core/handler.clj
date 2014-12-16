(ns newsnap.core.handler
  (:require [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.params :refer [wrap-params]]
            [hiccup.form :as form]
            [hiccup.page :refer [html5 include-css]]
            [hiccup.util :refer [escape-html]]
            [newsnap.core.model :as model]
            [newsnap.core.schema :as schema])
  (:gen-class))

(def title "Newsnap")

(def form-test
  (form/form-to [:post "/"]
                (form/label "news" "what's your news?")
                (form/text-area "news")
                (form/submit-button "submit")))

(defn root [& body]
  (html5
    [:html
     [:head
      (include-css "simple.css")
      [:title title]]
     [:body body]]))

(defn news-form
  [news-text]
  [:div {:class "post"}
   [:p {:class "title"} "Breaking news: Peanuts peanut peanuts peanut!"]
   [:p {:class "post"} news-text]])

(defn all-news-dom
  []
  (let [news (model/all-news)]
    (into [:div] (map #(news-form (escape-html (:body %))) news))))
      

(defroutes app-routes
  (GET "/" [] (root form-test (all-news-dom)))
  (POST "/" [news] (model/create news))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (handler/site app-routes))

(defn start [port]
  (jetty/run-jetty app {:port port :join? false}))

(defn -main []
  (schema/migrate)
  (let [port (Integer. (or (System/getenv "PORT") "5000"))]
    (start port)))
