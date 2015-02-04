(ns newsnap.view.pages
  (:require [hiccup.page :refer [html5 include-css]]
            [hiccup.util :refer [escape-html]]
            [newsnap.view.forms :as forms]
            [newsnap.core.model :as model]))

(def title "Newsnap")

;; root html elements to be displayed in every page
;; includes logo and title
(defn root 
  [& body]
  (html5
    [:html
     [:head
      (include-css "simple.css")
   ;; [:img {:src "logo/newsnaplogo.png"}]
      [:div {:class "primary logo"}
       [:p {:class "logo"} "Newsnap"]
       [:p {:class "subtitle"} "Share Your News."]]
      [:title title]]
     [:body body]]))

;; list to be populated with elements
(def news-list
  [:ul {:class "news"}])

;; takes title from db query and returns hiccup data structure 
;; representing a list item to be inserted into the news-list
(defn title-to-list
  [query]
  [:li 
   [:a {:href (str "/" (:countdownkey query))}
    ;; must escape-html to properly view title
    (escape-html (:title query))]])

;; convert query into html row with a title from a thread and like/dislike icons
(defn title-to-row
  [query]
  [:tr {:class "primary-light"}
   [:td {:width "80%"} [:a {:href (str "/" (:countdownkey query))} (escape-html (:title query))]]
   [:td [:img {:src "images/like.png" :align "center"}]]
   [:td [:img {:src "images/dislike.png" :align "center"}]]])

(defn all-news-table
  []
  (let [news (model/all-news)]
    (into [:table {:style "width:100%"}] (map title-to-row news))))

(defn news-reply
  [query]
  [:div {:class "reply primary-light"}
   [:div {:class "poster-info"}
    (when-not (clojure.string/blank? (:title query))
      [:p {:class "title"} (escape-html (:title query))])
    [:p {:class "poster-name"} (if (clojure.string/blank? (:name query))
                                 "Anonymous"
                                 (escape-html (:name query)))]
    (when-not (clojure.string/blank? (:email query))
      [:p {:class "poster-email"} (escape-html (:email query))])]
   [:div {:class "poster-body"}
    (escape-html (:body query))]
   [:div {:class "poster-other"}
    ;TODO: quote info
    ]])

(defn news-post
  [id]
  (let [queries (model/news-item id)]
    (into [:div {:class "news-item primary"}] (map news-reply queries))))

(def main-page
  (root forms/new-thread-form (all-news-table)))

(defn thread-page
  [thread]
  (root (forms/reply-form (str "/" thread)) (news-post thread)))