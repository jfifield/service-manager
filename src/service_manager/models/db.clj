(ns service-manager.models.db
  (:require [korma.core :refer :all]
            [korma.db :refer [defdb]]
            [inflections.core :refer :all]))

(defdb db "sqlite:service-manager.db")

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

(defentity hosts)
(defentity environments)
(defentity keypairs)
(defentity services)

(defcrud hosts)
(defcrud environments)
(defcrud keypairs)
(defcrud services)

