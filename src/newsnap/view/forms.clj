(ns newsnap.view.forms
  (:require [hiccup.form :as form]
            [ring.util.anti-forgery :refer [anti-forgery-field]]))

(def new-thread-form
  [:div {:class "primary form"}
     (form/form-to [:post "/newthread"]
                   ;; MUST add this func to each form to prevent 
                   ;; "Invalid anti-forgery token" message since the 
                   ;; ring-defaults site-defaults wrappers has a 
                   ;; anti-forgery wrapper. read more here:
                   ;; https://github.com/ring-clojure/ring-anti-forgery
                   ;; good explanation for what's a CSRF attack:
                   ;; http://www.lispcast.com/clojure-web-security
                   (anti-forgery-field)
                   [:div 
                    [:label 
                     {:class "in-form" 
                      :for "op-name"} 
                     "Name:"]]
                   [:div 
                    [:input 
                     {:class "in-form" 
                      :type "text" 
                      :id "op-name" 
                      :name 
                      "op-name"}]]
                   [:div 
                    [:label 
                     {:class "in-form" 
                      :for "op-email"} 
                     "Email:"]]
                   [:div 
                    [:input 
                     {:class "in-form" 
                      :type "op-email" 
                      :id "op-email" 
                      :name "op-email"}]]
                   [:div 
                    [:label 
                     {:class "in-form" 
                      :for "title"} 
                     "Title:"]]
                   [:div 
                    [:textarea 
                     {:class "in-form" 
                      :id "title" 
                      :name "title" 
                      :rows "1" 
                      :cols "50"}]]
                   [:div 
                    [:label 
                     {:class "in-form" 
                      :for "news"} 
                     "News:"]]
                   [:div 
                    [:textarea 
                     {:class "in-form" 
                      :id "news" 
                      :name "news" 
                      :rows "10" 
                      :cols "50"}]]
                   [:div 
                    [:input 
                     {:class "in-form primary-light btn" 
                      :type "submit" 
                      :value "submit"}]])])


(defn reply-form
  [route]
  [:div {:class "primary form"}
   (form/form-to [:post route]
                 (anti-forgery-field)
                 [:div 
                  [:label 
                   {:class "in-form" 
                    :for "replier-name"} 
                   "Name:"]]
                 [:div 
                  [:input 
                   {:class "in-form" 
                    :type "text" 
                    :id "replier-name" 
                    :name "replier-name"}]]
                 [:div 
                  [:label 
                   {:class "in-form" 
                    :for "replier-email"} 
                   "Email:"]]
                 [:div 
                  [:input 
                   {:class "in-form" 
                    :type "replier-email" 
                    :id "replier-email" 
                    :name "replier-email"}]]
                 [:div 
                  [:label 
                   {:class "in-form" 
                    :for "reply"} 
                   "Reply:"]]
                 [:div 
                  [:textarea 
                   {:class "in-form" 
                    :id "reply" 
                    :name "reply" 
                    :rows "10" 
                    :cols "50"}]]
                 [:div 
                  [:input 
                   {:class "in-form primary-light btn" 
                    :type "submit" 
                    :value "submit"}]])])
