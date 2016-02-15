(ns service-manager.routes.home
  (:require [compojure.core :refer :all]
            [service-manager.views.layout :as layout]))

(defn home []
  (layout/common [:h1 "Hello World!"]))

(defroutes home-routes
  (GET "/" [] (home)))
