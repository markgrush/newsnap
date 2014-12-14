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

(defn form-test
  []
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

(def some-text
  "Peanuts is a syndicated daily and Sunday American comic strip written and illustrated by Charles M. Schulz, which ran from October 2, 1950, to February 13, 2000, continuing in reruns afterward. The strip is the most popular and influential in the history of comic strips, with 17,897 strips published in all,[1] making it \"arguably the longest story ever told by one human being\".[2] At its peak, Peanuts ran in over 2,600 newspapers, with a readership of 355 million in 75 countries, and was translated into 21 languages.[3] It helped to cement the four-panel gag strip as the standard in the United States,[4] and together with its merchandise earned Schulz more than $1 billion.[1] Reprints of the strip are still syndicated and run in almost every U.S. newspaper.")
     
(defn news-form
  [news-text]
  [:div {:class "post"}
   [:p {:class "title"} "Breaking news: Peanuts peanut peanuts peanut!"]
   [:p {:class "post"} news-text]])

(defn all-news-dom
  []
  (let [news (model/all-news)]
    (map #([:div {:class "post"} %]) news)))
      

(defroutes app-routes
  (GET "/" [] (root form-test (all-news-dom)))
  (POST "/" [news] (model/create news))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (handler/site app-routes))

(defn -main []
  (schema/migrate)
  (let [port (Integer/parseInt (get (System/getenv) "PORT" "5000"))]
    (jetty/run-jetty app-routes {:port port})))
