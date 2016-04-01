(ns service-manager.routes.services
  (:require [compojure.core :refer :all]
            [clojure.set :refer [difference]]
            [ring.util.response :as response]
            [service-manager.views.layout :as layout]
            [service-manager.views.form :refer :all]
            [service-manager.views.status :refer [host-service-status]]
            [service-manager.models.db :as db]))

(defn list-services-page []
  (let [services (db/get-services)]
    (layout/common
      :services
      [:div.pull-right {:style "margin-bottom: 10px;"}
       [:a.btn.btn-default {:href "/services/new"}
        [:span.glyphicon.glyphicon-plus] " Add Service"]]
      [:table.table
       [:tr
        [:th "Name"]
        (repeat 3 [:th {:style "width: 1%;"}])]
       (for [service services]
         [:tr
          [:td (:name service)]
          [:td
           [:a.btn.btn-default {:href (str "/services/" (:id service))} "View"]]
          [:td
           [:a.btn.btn-default {:href (str "/services/" (:id service) "/edit")} "Edit"]]
          [:td
           [:form {:method "post" :action (str "/services/" (:id service))}
            [:input {:type "hidden" :name "_method" :value "delete"}]
            [:button.btn.btn-default {:type "submit"} "Delete"]]]])])))

(defn add-service-page []
  (layout/common
    :services
    [:form {:method "post" :action "/services"}
     (map #(text-field % {}) [:name :start_command :stop_command :status_command])
     [:button.btn.btn-default {:type "submit"}
      [:span.glyphicon.glyphicon-ok] " Save"]
     [:a.btn.btn-default {:href "/services"}
      [:span.glyphicon.glyphicon-remove] " Cancel"]]))

(defn edit-service-page [id]
  (let [service (db/get-service id)]
    (layout/common
      :services
      [:form {:method "post" :action (str "/services/" id)}
       [:input {:type "hidden" :name "_method" :value "put"}]
       (map #(text-field % service) [:name :start_command :stop_command :status_command])
       [:button.btn.btn-default {:type "submit"}
        [:span.glyphicon.glyphicon-ok] " Save"]
       [:a.btn.btn-default {:href "/services"}
        [:span.glyphicon.glyphicon-remove] " Cancel"]])))

(defn save-service [params]
  (let [service (select-keys params [:name :start_command :stop_command :status_command])]
    (db/save-service service)
    (response/redirect "/services")))

(defn update-service [id params]
  (let [service (select-keys params [:name :start_command :stop_command :status_command])]
    (db/update-service id service)
    (response/redirect "/services")))

(defn view-service-summary [id]
  (let [service (db/get-service id)]
    (list
      [:h1 (:name service)]
      [:div.row
       [:div.col-md-2 [:strong "Start Command"]]
       [:div.col-md-10 (:start_command service)]]
      [:div.row
       [:div.col-md-2 [:strong "Stop Command"]]
       [:div.col-md-10 (:stop_command service)]]
      [:div.row
       [:div.col-md-2 [:strong "Status Command"]]
       [:div.col-md-10 (:status_command service)]])))

(defn view-service-hosts [id]
  (let [service-hosts (db/get-service-hosts id)]
    (list
      [:div.row.buffer-top
       [:div.col-md-6
        [:h4 "Hosts"]]
       [:div.col-md-6
        (when-let [hosts (not-empty (difference (set (db/get-hosts)) (set service-hosts)))]
          [:div.pull-right {:style "margin-bottom: 10px;"}
           [:form.form-inline {:method "post" :action(str "/services/" id "/hosts")}
            [:div.form-group
             [:label.sr-only {:for "host_id"} "Host"]
             [:select.form-control {:id "host_id" :name "host_id"}
              [:option]
              (for [host hosts]
                [:option {:value (:id host)} (:name host)])]]
            "&nbsp;"
            [:button.btn.btn-default {:type "submit"}
             [:span.glyphicon.glyphicon-plus] " Add Host"]]])]]
      [:table.table
       [:tr
        (map #(vector :th %) ["Name" "Status"])
        (repeat 3 [:th {:style "width: 1%;"}])]
       (for [host service-hosts]
         [:tr
          [:td (:name host)]
          (host-service-status :td (:id host) id)
          [:td
           [:button.btn.btn-default.host-service-start {:data-host-id (:id host) :data-service-id id} "Start"]]
          [:td
           [:button.btn.btn-default.host-service-stop {:data-host-id (:id host) :data-service-id id} "Stop"]]
          [:td
           [:form {:method "post" :action (str "/services/" id "/hosts/" (:id host))}
            [:input {:type "hidden" :name "_method" :value "delete"}]
            [:button.btn.btn-default {:type "submit"} "Delete"]]]])])))

(defn view-service [id]
  (layout/common
    :services
    (view-service-summary id)
    (view-service-hosts id)))

(defn delete-service [id]
  (db/delete-service id)
  (response/redirect "/services"))

(defn add-host-service [host-id service-id]
  (db/add-host-service host-id service-id)
  (response/redirect (str "/services/" service-id)))

(defn remove-host-service [host-id service-id]
  (db/remove-host-service host-id service-id)
  (response/redirect (str "/services/" service-id)))

(defroutes services-routes
  (context "/services" []
           (GET "/" [] (list-services-page))
           (GET "/new" [] (add-service-page))
           (POST "/" [& params] (save-service params))
           (GET "/:id" [id] (view-service id))
           (GET "/:id/edit" [id] (edit-service-page id))
           (PUT "/:id" [id & params] (update-service id params))
           (DELETE "/:id" [id] (delete-service id))
           (POST "/:id/hosts" [id host_id] (add-host-service host_id id))
           (DELETE "/:id/hosts/:host_id" [id host_id] (remove-host-service host_id id))))
