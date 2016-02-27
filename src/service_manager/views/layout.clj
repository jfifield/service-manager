(ns service-manager.views.layout
  (:require [hiccup.page :refer [html5 include-css include-js]]))

(def nav-items
  [{:id :home :href "/" :title "Home"}
   {:id :hosts :href "/hosts" :title "Hosts"}
   {:id :environments :href "/environments" :title "Environments"}
   {:id :keypairs :href "/keypairs" :title "Key Pairs"}
   {:id :services :href "/services" :title "Services"}])

(defn nav-bar [active-nav-item]
  [:nav.navbar.navbar-default.navbar-fixed-top
   [:div.container
    [:div.navbar-header
     [:button.navbar-toggle.collapsed {:type "button" :data-toggle "collapse" :data-target "#navbar" :aria-expanded "false"}
      [:span.sr-only "Toggle navigation"]
      (repeat 3 [:span.icon-bar])]
     [:a.navbar-brand {:href "/"} "Service Manager"]]
    [:div#navbar.collapse.navbar-collapse
     [:ul.nav.navbar-nav
      (map (fn [nav-item]
             [:li (if (= (:id nav-item) active-nav-item) {:class "active"})
              [:a {:href (:href nav-item)} (:title nav-item)]])
           nav-items)]]]])

(defn common [active-nav-item & body]
  (html5
    [:head
     [:title "Service Manager"]
     (include-css "/assets/bootstrap/css/bootstrap.min.css")
     (include-css "/css/screen.css")]
    [:body
     (nav-bar active-nav-item)
     [:div.container body]
     (include-js "/assets/jquery/jquery.min.js")
     (include-js "/assets/bootstrap/js/bootstrap.min.js")
     (include-js "/js/main.js")]))
