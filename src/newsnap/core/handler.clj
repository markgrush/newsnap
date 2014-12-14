(ns newsnap.core.handler
  (:require [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.adapter.jetty :as jetty]
            [hiccup.form :as form]
            [hiccup.page :refer [html5 include-css]]
            [newsnap.core.model :as model]
            [newsnap.core.schema :as schema]))

(def title "Newsnap")

(def form-test
  (form/form-to [:post "/"]
                (form/label "news" "what's your news?")
                (form/text-field "news")
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
    (last (map #(news-form (:body %)) news))))
      

(defroutes app-routes
  (GET "/" [] (root form-test (all-news-dom)))
  (POST "/" [news] (str "the news: " news))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (handler/site app-routes))

(defn -main []
  (schema/migrate)
  (let [port (Integer/parseInt (get (System/getenv) "PORT" "5000"))]
    (jetty/run-jetty app-routes {:port port})))
