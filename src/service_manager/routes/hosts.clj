(ns service-manager.routes.hosts
  (:require [compojure.core :refer :all]
            [ring.util.response :as response]
            [hiccup.core :refer [html]]
            [service-manager.views.layout :as layout]
            [service-manager.views.form :refer :all]
            [service-manager.models.db :as db]
            [service-manager.ssh :as ssh]))

(defn list-hosts-page []
  (let [hosts (db/get-hosts)]
    (layout/common
      :hosts
      [:div.pull-right {:style "margin-bottom: 10px;"}
       [:a.btn.btn-default {:href "/hosts/new"}
        [:span.glyphicon.glyphicon-plus] " Add Host"]]
      [:table.table
       [:tr
        (map #(vector :th %) ["Name" "Address" "Username"])
        (repeat 3 [:th {:style "width: 1%;"}])]
       (for [host hosts]
         [:tr
          (map #(vector :td (% host)) [:name :address :username])
          [:td
           [:a.btn.btn-default {:href (str "/hosts/" (:id host))} "View"]]
          [:td
           [:a.btn.btn-default {:href (str "/hosts/" (:id host) "/edit")} "Edit"]]
          [:td
           [:form {:method "post" :action (str "/hosts/" (:id host))}
            [:input {:type "hidden" :name "_method" :value "delete"}]
            [:button.btn.btn-default {:type "submit"} "Delete"]]]])])))

(defn add-host-page []
  (let [environments (db/get-environments)
        keypairs (db/get-keypairs)]
    (layout/common
      :hosts
      [:form {:method "post" :action "/hosts"}
       (map #(text-field % {}) [:name :address :username])
       (select-field :environment_id {} environments {:title "Environment"})
       (select-field :keypair_id {} keypairs {:title "Key Pair"})
       [:button.btn.btn-default {:type "submit"}
        [:span.glyphicon.glyphicon-ok] " Save"]
       [:a.btn.btn-default {:href "/hosts"}
        [:span.glyphicon.glyphicon-remove " Cancel"]]])))

(defn edit-host-page [id]
  (let [host (db/get-host id)
        environments (db/get-environments)
        keypairs (db/get-keypairs)]
    (layout/common
      :hosts
      [:form {:method "post" :action (str "/hosts/" id)}
       [:input {:type "hidden" :name "_method" :value "put"}]
       (map #(text-field % host) [:name :address :username])
       (select-field :environment_id host environments {:title "Environment"})
       (select-field :keypair_id host keypairs {:title "Key Pair"})
       [:button.btn.btn-default {:type "submit"}
        [:span.glyphicon.glyphicon-ok] " Save"]
       [:a.btn.btn-default {:href "/hosts"}
        [:span.glyphicon.glyphicon-remove " Cancel"]]])))

(defn save-host [params]
  (let [host (select-keys params [:name :address :username :environment_id :keypair_id])]
    (db/save-host host)
    (response/redirect "/hosts")))

(defn update-host [id params]
  (let [host (select-keys params [:name :address :username :environment_id :keypair_id])]
    (db/update-host id host)
    (response/redirect "/hosts")))

(defn view-host-summary [id]
  (let [host (db/get-host id)
        environment (db/get-environment (:environment_id host))
        keypair (db/get-keypair (:keypair_id host))]
    (list
      [:h1 (:name host)]
      [:div.row
       [:div.col-md-2 [:strong "Status"]]
       [:div.col-md-10.host-status {:data-host-id id}
        [:span.text-warning [:span.glyphicon.glyphicon-question-sign] " Checking..."]]]
      [:div.row
       [:div.col-md-2 [:strong "Address"]]
       [:div.col-md-10 (:address host)]]
      [:div.row
       [:div.col-md-2 [:strong "Username"]]
       [:div.col-md-10 (:username host)]]
      [:div.row
       [:div.col-md-2 [:strong "Environment"]]
       [:div.col-md-10 (:name environment)]]
      [:div.row
       [:div.col-md-2 [:strong "Key Pair"]]
       [:div.col-md-10 (:name keypair)]])))

(defn view-host-services [id]
  (let [host-services (db/get-host-services id)
        all-services (db/get-services)
        services (remove #(contains? (set (map :id host-services)) (:id %)) all-services)]
    (list
      [:h2 "Services"]
      [:div.pull-right {:style "margin-bottom: 10px;"}
       [:form.form-inline {:method "post" :action(str "/hosts/" id "/services")}
        [:div.form-group
         [:label.sr-only {:for "service_id"} "Service"]
         [:select.form-control {:id "service_id" :name "service_id"}
          [:option]
          (for [service services]
            [:option {:value (:id service)} (:name service)])]]
        "&nbsp;"
        [:button.btn.btn-default {:type "submit"}
         [:span.glyphicon.glyphicon-plus] " Add Service"]]]
      [:table.table
       [:tr
        [:th "Name"]
        [:th {:style "width: 1%;"}]]
       (for [service host-services]
         [:tr
          [:td (:name service)]
          [:td
           [:form {:method "post" :action (str "/hosts/" id "/services/" (:id service))}
            [:input {:type "hidden" :name "_method" :value "delete"}]
            [:button.btn.btn-default {:type "submit"} "Delete"]]]])])))

(defn view-host [id]
  (layout/common
    :hosts
    (view-host-summary id)
    (view-host-services id)))

(defn delete-host [id]
  (db/delete-host id)
  (response/redirect "/hosts"))

(defn get-host-status [id]
  (let [host (db/get-host id)
        keypair (db/get-keypair (:keypair_id host))]
    (html
      (try
        (let [result (ssh/exec-ssh-cmd host keypair "echo \"OK\"")]
          [:span.text-success [:span.glyphicon.glyphicon-ok-sign] " " (:out result)])
        (catch com.jcraft.jsch.JSchException e
          [:span.text-danger [:span.glyphicon.glyphicon-remove-sign] " "
           (if-let [cause (.getCause e)]
             (condp instance? cause
               java.net.UnknownHostException "Unknown host"
               java.net.NoRouteToHostException "No route to host"
               (.getMessage cause))
             (.getMessage e))])))))

(defn add-host-service [host-id service-id]
  (db/add-host-service host-id service-id)
  (response/redirect (str "/hosts/" host-id)))

(defn remove-host-service [host-id service-id]
  (db/remove-host-service host-id service-id)
  (response/redirect (str "/hosts/" host-id)))

(defroutes hosts-routes
  (context "/hosts" []
           (GET "/" [] (list-hosts-page))
           (GET "/new" [] (add-host-page))
           (POST "/" [& params] (save-host params))
           (GET "/:id" [id] (view-host id))
           (GET "/:id/edit" [id] (edit-host-page id))
           (PUT "/:id" [id & params] (update-host id params))
           (DELETE "/:id" [id] (delete-host id))
           (GET "/:id/status" [id] (get-host-status id))
           (POST "/:id/services" [id service_id] (add-host-service id service_id))
           (DELETE "/:id/services/:service_id" [id service_id] (remove-host-service id service_id))))
