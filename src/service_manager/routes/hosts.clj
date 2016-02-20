(ns service-manager.routes.hosts
  (:require [compojure.core :refer :all]
            [clojure.string :as str]
            [ring.util.response :as response]
            [service-manager.views.layout :as layout]
            [service-manager.models.db :as db]))

(defn list-hosts-page []
  (let [hosts (db/get-hosts)]
    (layout/common
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

(defn text-field [f o]
  (let [title (str/capitalize (name f))]
    [:div.form-group
     [:label {:for f} title]
     [:input.form-control {:type "text" :id f :name f :placeholder title :value (f o)}]]))

(defn add-host-page []
  (layout/common
    [:form {:method "post" :action "/hosts"}
     (map #(text-field % {}) [:name :address :username])
     [:button.btn.btn-default {:type "submit"}
      [:span.glyphicon.glyphicon-ok] " Save"]
     [:a.btn.btn-default {:href "/hosts"}
      [:span.glyphicon.glyphicon-remove " Cancel"]]]))

(defn edit-host-page [id]
  (let [host (db/get-host id)]
    (layout/common
     [:form {:method "post" :action (str "/hosts/" id)}
      [:input {:type "hidden" :name "_method" :value "put"}]
      (map #(text-field % host) [:name :address :username])
      [:button.btn.btn-default {:type "submit"}
       [:span.glyphicon.glyphicon-ok] " Save"]
      [:a.btn.btn-default {:href "/hosts"}
       [:span.glyphicon.glyphicon-remove " Cancel"]]])))

(defn save-host [params]
  (let [host (select-keys params [:name :address :username])]
    (db/save-host host)
    (response/redirect "/hosts")))

(defn update-host [id params]
  (let [host (select-keys params [:name :address :username])]
    (db/update-host id host)
    (response/redirect "/hosts")))

(defn view-host [id]
  (let [host (db/get-host id)]
    (layout/common
      [:h1 (:name host)]
      [:div.row
       [:div.col-md-2 [:strong "Address"]]
       [:div.col-md-10 (:address host)]]
      [:div.row
       [:div.col-md-2 [:strong "Username"]]
       [:div.col-md-10 (:username host)]])))

(defn delete-host [id]
  (db/delete-host id)
  (response/redirect "/hosts"))

(defroutes hosts-routes
  (context "/hosts" []
           (GET "/" [] (list-hosts-page))
           (GET "/new" [] (add-host-page))
           (POST "/" [& params] (save-host params))
           (GET "/:id" [id] (view-host id))
           (GET "/:id/edit" [id] (edit-host-page id))
           (PUT "/:id" [id & params] (update-host id params))
           (DELETE "/:id" [id] (delete-host id))))
