(ns service-manager.routes.auth
  (:require [compojure.core :refer :all]
            [ring.util.response :refer [redirect]]
            [service-manager.views.layout :as layout]
            [service-manager.models.db :as db]))

(defn login-form [error]
  (layout/basic
    [:div.row
     [:div.col-md-offset-4.col-md-4
      (if error [:div.alert.alert-danger error])
      [:div.panel.panel-default
       [:div.panel-body
        [:form {:method "POST"}
         [:div.form-group
          [:label {:for "username"} "Username"]
          [:input.form-control {:type "text" :name "username"}]]
         [:div.form-group
          [:label {:for "password"} "Password"]
          [:input.form-control {:type "password" :name "password"}]]
         [:button.btn.btn-default {:type "submit"} "Submit"]]]]]]))

(defn login [request]
  (let [username (get-in request [:form-params "username"])
        password (get-in request [:form-params "password"])]
    (if (db/authenticate-user username password)
      (let [session (:session request)
            updated-session (assoc session :identity (keyword username))]
        (-> (redirect "/")
            (assoc :session updated-session)))
      (login-form "Login failed"))))

(defn logout []
  (-> (redirect "/login")
      (assoc :session {})))

(defroutes auth-routes
  (GET "/login" [] (login-form nil))
  (POST "/login" request (login request))
  (ANY "/logout" [] (logout)))
