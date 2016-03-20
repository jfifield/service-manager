(ns service-manager.routes.users
  (:require [compojure.core :refer :all]
            [ring.util.response :as response]
            [service-manager.views.layout :as layout]
            [service-manager.views.form :refer :all]
            [service-manager.models.db :as db]))

(defn list-users-page []
  (let [users (db/get-users)]
    (layout/common
      :users
      [:div.pull-right {:style "margin-bottom: 10px;"}
       [:a.btn.btn-default {:href "/users/new"}
        [:span.glyphicon.glyphicon-plus] " Add User"]]
      [:table.table
       [:tr
        [:th "Username"]
        (repeat 3 [:th {:style "width: 1%;"}])]
       (for [user users]
         [:tr
          [:td (:username user)]
          [:td
           [:a.btn.btn-default {:href (str "/users/" (:id user))} "View"]]
          [:td
           [:a.btn.btn-default {:href (str "/users/" (:id user) "/edit")} "Edit"]]
          [:td
           [:form {:method "post" :action (str "/users/" (:id user))}
            [:input {:type "hidden" :name "_method" :value "delete"}]
            [:button.btn.btn-default {:type "submit"} "Delete"]]]])])))

(defn add-user-page []
  (layout/common
    :users
    [:form {:method "post" :action "/users"}
     (text-field :username {})
     (password-field :password)
     [:button.btn.btn-default {:type "submit"}
      [:span.glyphicon.glyphicon-ok] " Save"]
     [:a.btn.btn-default {:href "/users"}
      [:span.glyphicon.glyphicon-remove " Cancel"]]]))

(defn edit-user-page [id]
  (let [user (db/get-user id)]
    (layout/common
      :users
      [:form {:method "post" :action (str "/users/" id)}
       [:input {:type "hidden" :name "_method" :value "put"}]
       (text-field :username user)
       (password-field :password)
       [:button.btn.btn-default {:type "submit"}
        [:span.glyphicon.glyphicon-ok] " Save"]
       [:a.btn.btn-default {:href "/users"}
        [:span.glyphicon.glyphicon-remove " Cancel"]]])))

(defn save-user [params]
  (let [user (select-keys params [:username :password])]
    (db/save-user user)
    (response/redirect "/users")))

(defn update-user [id params]
  (let [user (select-keys params [:username :password])]
    (db/update-user id user)
    (response/redirect "/users")))

(defn view-user-summary [id]
  (let [user (db/get-user id)]
    [:h1 (:username user)]))

(defn view-user [id]
  (layout/common
    :users
    (view-user-summary id)))

(defn delete-user [id]
  (db/delete-user id)
  (response/redirect "/users"))

(defroutes users-routes
  (context "/users" []
           (GET "/" [] (list-users-page))
           (GET "/new" [] (add-user-page))
           (POST "/" [& params] (save-user params))
           (GET "/:id" [id] (view-user id))
           (GET "/:id/edit" [id] (edit-user-page id))
           (PUT "/:id" [id & params] (update-user id params))
           (DELETE "/:id" [id] (delete-user id))))
