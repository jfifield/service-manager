(ns service-manager.views.form
  (:require [inflections.core :refer [titleize]]))

(defn text-field [field-name entity]
  (let [title (titleize (name field-name))]
    [:div.form-group
     [:label {:for field-name} title]
     [:input.form-control {:type "text" :id field-name :name field-name :placeholder title :value (field-name entity)}]]))

(defn password-field [field-name]
  (let [title (titleize (name field-name))]
    [:div.form-group
     [:label {:for field-name} title]
     [:input.form-control {:type "password" :id field-name :name field-name :placeholder title}]]))

(defn textarea-field [field-name entity]
  (let [title (titleize (name field-name))]
    [:div.form-group
     [:label {:for field-name} title]
     [:textarea.form-control {:id field-name :name field-name :rows 10 :placeholder title} (field-name entity)]]))

(defn select-field [field-name entity option-entities opts]
  (let [title (:title opts (titleize (name field-name)))]
    [:div.form-group
     [:label {:for field-name} title]
     [:select.form-control {:id field-name :name field-name}
      [:option]
      (for [option-entity option-entities]
        [:option {:value (:id option-entity) :selected (= (:id option-entity) (field-name entity))} (:name option-entity)])]]))

