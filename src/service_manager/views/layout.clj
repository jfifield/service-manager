(ns service-manager.views.layout
  (:require [hiccup.page :refer [html5 include-css include-js]]))

(defn nav-bar []
  [:nav.navbar.navbar-default.navbar-fixed-top
   [:div.container
    [:div.navbar-header
     [:button.navbar-toggle.collapsed {:type "button" :data-toggle "collapse" :data-target "#navbar" :aria-expanded "false"}
      [:span.sr-only "Toggle navigation"]
      (repeat 3 [:span.icon-bar])]
     [:a.navbar-brand {:href "#"} "Service Manager"]]
    [:div#navbar.collapse.navbar-collapse
     [:ul.nav.navbar-nav
      [:li.active [:a {:href "#"} "Link 1"]]
      [:li [:a {:href "/hosts"} "Hosts"]]
      [:li [:a {:href "/keypairs"} "Key Pairs"]]]]]])

(defn common [& body]
  (html5
    [:head
     [:title "Service Manager"]
     (include-css "/assets/bootstrap/css/bootstrap.min.css")
     (include-css "/css/screen.css")]
    [:body
     (nav-bar)
     [:div.container body]
     (include-js "/assets/jquery/jquery.min.js")
     (include-js "/assets/bootstrap/js/bootstrap.min.js")]))
