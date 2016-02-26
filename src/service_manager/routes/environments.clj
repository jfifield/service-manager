(ns service-manager.routes.environments
  (:require [compojure.core :refer :all]
            [ring.util.response :as response]
            [service-manager.views.layout :as layout]
            [service-manager.views.form :refer :all]
            [service-manager.models.db :as db]))

(defn list-environments-page []
  (let [environments (db/get-environments)]
    (layout/common
      :environments
      [:div.pull-right {:style "margin-bottom: 10px;"}
       [:a.btn.btn-default {:href "/environments/new"}
        [:span.glyphicon.glyphicon-plus] " Add Environment"]]
      [:table.table
       [:tr
        [:th "Name"]
        (repeat 3 [:th {:style "width: 1%;"}])]
       (for [environment environments]
         [:tr
          [:td (:name environment)]
          [:td
           [:a.btn.btn-default {:href (str "/environments/" (:id environment))} "View"]]
          [:td
           [:a.btn.btn-default {:href (str "/environments/" (:id environment) "/edit")} "Edit"]]
          [:td
           [:form {:method "post" :action (str "/environments/" (:id environment))}
            [:input {:type "hidden" :name "_method" :value "delete"}]
            [:button.btn.btn-default {:type "submit"} "Delete"]]]])])))

(defn add-environment-page []
  (layout/common
    :environments
    [:form {:method "post" :action "/environments"}
     (text-field :name {})
     [:button.btn.btn-default {:type "submit"}
      [:span.glyphicon.glyphicon-ok] " Save"]
     [:a.btn.btn-default {:href "/environments"}
      [:span.glyphicon.glyphicon-remove " Cancel"]]]))

(defn edit-environment-page [id]
  (let [environment (db/get-environment id)]
    (layout/common
      :environments
      [:form {:method "post" :action (str "/environments/" id)}
       [:input {:type "hidden" :name "_method" :value "put"}]
       (text-field :name environment)
       [:button.btn.btn-default {:type "submit"}
        [:span.glyphicon.glyphicon-ok] " Save"]
       [:a.btn.btn-default {:href "/environments"}
        [:span.glyphicon.glyphicon-remove " Cancel"]]])))

(defn save-environment [params]
  (let [environment (select-keys params [:name])]
    (db/save-environment environment)
    (response/redirect "/environments")))

(defn update-environment [id params]
  (let [environment (select-keys params [:name])]
    (db/update-environment id environment)
    (response/redirect "/environments")))

(defn view-environment-summary [id]
  (let [environment (db/get-environment id)]
    [:h1 (:name environment)]))

(defn view-environment-hosts [id]
  (let [hosts (db/get-environment-hosts id)]
    (list
      [:h2 "Hosts"]
      [:table.table
       [:tr
        [:th "Name"]]
       (for [host hosts]
         [:tr
          [:td (:name host)]])])))

(defn view-environment [id]
  (layout/common
    :environments
    (view-environment-summary id)
    (view-environment-hosts id)))

(defn delete-environment [id]
  (db/delete-environment id)
  (response/redirect "/environments"))

(defroutes environments-routes
  (context "/environments" []
           (GET "/" [] (list-environments-page))
           (GET "/new" [] (add-environment-page))
           (POST "/" [& params] (save-environment params))
           (GET "/:id" [id] (view-environment id))
           (GET "/:id/edit" [id] (edit-environment-page id))
           (PUT "/:id" [id & params] (update-environment id params))
           (DELETE "/:id" [id] (delete-environment id))))
