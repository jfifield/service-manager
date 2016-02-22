(ns service-manager.handler
  (:require [compojure.core :refer [defroutes routes]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.file-info :refer [wrap-file-info]]
            [ring.middleware.webjars :refer [wrap-webjars]]
            [hiccup.middleware :refer [wrap-base-url]]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [service-manager.routes.home :refer [home-routes]]
            [service-manager.routes.hosts :refer [hosts-routes]]
            [service-manager.routes.environments :refer [environments-routes]]
            [service-manager.routes.keypairs :refer [keypairs-routes]]))

(defn init []
  (println "service-manager is starting"))

(defn destroy []
  (println "service-manager is shutting down"))

(defroutes app-routes
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (-> (routes
        home-routes
        hosts-routes
        environments-routes
        keypairs-routes
        app-routes)
      (handler/site)
      (wrap-base-url)
      wrap-webjars))
