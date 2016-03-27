(ns service-manager.models.db
  (:require [korma.core :refer :all]
            [korma.db :refer [defdb]]
            [inflections.core :refer :all]
            [clojure.string :refer [blank?]]
            [clojure.tools.logging :as log]
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
         (defn ~(symbol (str "get-" singular-entity "-count")) []
           (:c (first (select ~entity (aggregate (~'count :id) :c)))))
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

(alter-var-root
  #'save-user
  (fn [original-fn]
    (fn [user]
      (let [updated-user (assoc user :password (hashers/encrypt (:password user)))]
        (original-fn updated-user)))))

(alter-var-root
  #'update-user
  (fn [original-fn]
    (fn [id user]
      (let [updated-user (if (blank? (:password user))
                           (dissoc user :password)
                           (assoc user :password (hashers/encrypt (:password user))))]
        (original-fn id updated-user)))))

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
  (migratus/migrate {:store :database :db db-url})
  (if (= (get-user-count) 0)
    (do
      (log/info "Adding default admin user")
      (save-user {:username "admin" :password "admin"}))))
