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
  [:div {:class "primary form"}
     (form/form-to [:post "/"]
                [:div [:label {:class "in-form" :for "op-name"} "Name:"]]
                [:div [:input {:class "in-form" :type "text" :id "op-name" :name "op-name"}]]
                [:div [:label {:class "in-form" :for "op-email"} "Email:"]]
                [:div [:input {:class "in-form" :type "op-email" :id "op-email" :name "op-email"}]]
                [:div [:label {:class "in-form" :for "title"} "Title:"]]
                [:div [:textarea {:class "in-form" :id "title" :name "title" :rows "1" :cols "50"}]]
                [:div [:label {:class "in-form" :for "news"} "News:"]]
                [:div [:textarea {:class "in-form" :id "news" :name "news" :rows "10" :cols "50"}]]
                [:div [:input {:class "in-form primary-light btn" :type "submit" :value "submit"}]])])

(defn root 
  [& body]
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

(def news-list
  [:ul {:class "news"}])

(defn title-to-list
  [query]
  [:li [:a {:href (str "/" (:countdownkey query))} (escape-html (:title query))]])

(defn all-news-dom
  []
  (let [news (model/all-news)]
    (into news-list (map title-to-list news))))

(defn news-reply
  [query]
  [:div {:class "reply"}
   [:div {:class "poster-info"}
    [:p {:class "poster-name"} (if (clojure.string/blank? (:name query))
                                 "Anonymous"
                                 (:name query))]
    [:p {:class "poster-email"} (if (clojure.string/blank? (:email query))
                                  "-"
                                  (:email query))]]
   [:div {:class "poster-body"}
    (:body query)]
   [:div {:class "poster-other"}
    ;TODO: quote info
    ]])

(defn news-post
  [id]
  (let [queries (model/news-item id)]
    (into [:div {:class "news-item"}] (map news-reply queries))))
      
(defroutes app-routes
  (GET "/" [] (root form-test (all-news-dom)))
  (POST "/" [op-name op-email title news] (model/create op-name op-email title news))
  (GET "/:id" [id] (root (news-post id)))
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
