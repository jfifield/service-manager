(ns service-manager.models.db
  (:require [korma.core :refer :all]
            [korma.db :refer [defdb]]))

(defdb db "sqlite:service-manager.db")

