(ns service-manager.handler
  (:require [compojure.core :refer [defroutes routes]]
            [clojure.tools.logging :as log]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.file-info :refer [wrap-file-info]]
            [ring.middleware.webjars :refer [wrap-webjars]]
            [ring.util.response :refer [redirect]]
            [hiccup.middleware :refer [wrap-base-url]]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [buddy.auth :refer [authenticated?]]
            [buddy.auth.backends.session :refer [session-backend]]
            [buddy.auth.middleware :refer [wrap-authentication]]
            [buddy.auth.accessrules :refer [wrap-access-rules]]
            [service-manager.models.db :as db]
            [service-manager.request :refer [wrap-request-binding]]
            [service-manager.routes.auth :refer [auth-routes]]
            [service-manager.routes.home :refer [home-routes]]
            [service-manager.routes.users :refer [users-routes]]
            [service-manager.routes.hosts :refer [hosts-routes]]
            [service-manager.routes.environments :refer [environments-routes]]
            [service-manager.routes.keypairs :refer [keypairs-routes]]
            [service-manager.routes.services :refer [services-routes]]))

(defn init []
  (log/info "service-manager is starting")
  (db/init))

(defn destroy []
  (log/info "service-manager is shutting down"))

(defroutes app-routes
  (route/resources "/")
  (route/not-found "Not Found"))

(def access-rules
  {:rules [{:uris ["/login" "*.*"] :handler (fn [_] true)}
           {:uri "*" :handler authenticated?}]
   :on-error (fn [_ _] (redirect "/login"))})

(def app
  (-> (routes
        auth-routes
        home-routes
        hosts-routes
        users-routes
        environments-routes
        keypairs-routes
        services-routes
        app-routes)
      (wrap-request-binding)
      (wrap-access-rules access-rules)
      (wrap-authentication (session-backend))
      (handler/site)
      (wrap-base-url)
      wrap-webjars))
