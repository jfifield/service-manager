(ns service-manager.models.db
  (:require [korma.core :refer :all]
            [korma.db :refer [defdb]]))

(defdb db "sqlite:service-manager.db")

(defentity hosts)

(defn get-hosts []
  (select hosts))

(defn get-host [id]
  (first (select hosts (where {:id id}))))

(defn save-host [host]
  (insert hosts (values host)))

(defn update-host [id host]
  (update hosts (set-fields host) (where {:id id})))

(defn delete-host [id]
  (delete hosts (where {:id id})))
