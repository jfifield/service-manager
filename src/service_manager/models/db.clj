(ns service-manager.models.db
  (:require [korma.core :refer :all]
            [korma.db :refer [defdb]]
            [inflections.core :refer :all]
            [clojure.string :refer [blank?]]
            [buddy.hashers :as hashers]
            [migratus.core :as migratus]))

(def db-url "sqlite:service-manager.db")
(defdb db db-url)

(defmacro defcrud [entity]
  (let [singular-entity (singular (name entity))
        plural-entity (plural (name entity))]
    `(do (defn ~(symbol (str "get-" plural-entity)) []
           (select ~entity))
         (defn ~(symbol (str "get-" singular-entity)) [~'id]
           (first (select ~entity (where {:id ~'id}))))
         (defn ~(symbol (str "save-" singular-entity)) [~(symbol singular-entity)]
           (insert ~entity (values ~(symbol singular-entity))))
         (defn ~(symbol (str "update-" singular-entity)) [~'id ~(symbol singular-entity)]
           (update ~entity (set-fields ~(symbol singular-entity)) (where {:id ~'id})))
         (defn ~(symbol (str "delete-" singular-entity)) [~'id]
           (delete ~entity (where {:id ~'id}))))))

(declare users hosts environments keypairs services)

(defentity users)

(defentity hosts
  (many-to-many services :hosts_services {:lfk :host_id :rfk :service_id}))

(defentity environments
  (has-many hosts {:fk :environment_id}))

(defentity keypairs)

(defentity services
  (many-to-many hosts :hosts_services {:lfk :service_id :rfk :host_id}))

(defcrud hosts)
(defcrud users)
(defcrud environments)
(defcrud keypairs)
(defcrud services)

(defn authenticate-user [username password]
  (if-let [user (first (select users (where {:username username})))]
    (if (hashers/check password (:password user))
      user)))

(def save-user-internal save-user)

(defn save-user [user]
  (let [updated-user (assoc user :password (hashers/encrypt (:password user)))]
    (save-user-internal updated-user)))

(def update-user-internal update-user)

(defn update-user [id user]
  (let [updated-user (if (blank? (:password user))
                       (dissoc user :password)
                       (assoc user :password (hashers/encrypt (:password user))))]
    (update-user-internal id updated-user)))

(defn get-host-services [host-id]
  (-> (select hosts (with services) (where {:id host-id}))
      first
      :services))

(defn get-service-hosts [service-id]
  (-> (select services (with hosts) (where {:id service-id}))
      first
      :hosts))

(defn add-host-service [host-id service-id]
  (insert :hosts_services (values {:host_id host-id :service_id service-id})))

(defn remove-host-service [host-id service-id]
  (delete :hosts_services (where {:host_id host-id :service_id service-id})))

(defn get-environment-hosts [id]
  (-> (select environments (with hosts) (where {:id id}))
      first
      :hosts))

(defn init []
  (migratus/migrate {:store :database :db db-url}))
