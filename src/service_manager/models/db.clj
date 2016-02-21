(ns service-manager.models.db
  (:require [korma.core :refer :all]
            [korma.db :refer [defdb]]))

(defdb db "sqlite:service-manager.db")

(defentity hosts)
(defentity keypairs)

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

(defn get-keypairs []
  (select keypairs))

(defn get-keypair [id]
  (first (select keypairs (where {:id id}))))

(defn save-keypair [keypair]
  (insert keypairs (values keypair)))

(defn update-keypair [id keypair]
  (update keypairs (set-fields keypair) (where {:id id})))

(defn delete-keypair [id]
  (delete keypairs (where {:id id})))

