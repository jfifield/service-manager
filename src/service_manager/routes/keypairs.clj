(ns service-manager.routes.keypairs
  (:require [compojure.core :refer :all]
            [inflections.core :refer [titleize]]
            [ring.util.response :as response]
            [service-manager.views.layout :as layout]
            [service-manager.models.db :as db]
            [clj-ssh.ssh :as ssh]))

(defn list-keypairs-page []
  (let [keypairs (db/get-keypairs)]
    (layout/common
     [:div.pull-right {:style "margin-bottom: 10px;"}
      [:a.btn.btn-default {:href "/keypairs/new"}
       [:span.glyphicon.glyphicon-plus] " Add Key Pair"]]
     [:table.table
      [:tr
       [:th "Name"]
       (repeat 3 [:th {:style "width: 1%;"}])]
      (for [keypair keypairs]
        [:tr
         [:td (:name keypair)]
         [:td
          [:a.btn.btn-default {:href (str "/keypairs/" (:id keypair))} "View"]]
         [:td
          [:a.btn.btn-default {:href (str "/keypairs/" (:id keypair) "/edit")} "Edit"]]
         [:td
          [:form {:method "post" :action (str "/keypairs/" (:id keypair))}
           [:input {:type "hidden" :name "_method" :value "delete"}]
           [:button.btn.btn-default {:type "submit"} "Delete"]]]])])))

(defn text-field [f o]
  (let [title (titleize (name f))]
    [:div.form-group
     [:label {:for f} title]
     [:input.form-control {:type "text" :id f :name f :placeholder title :value (f o)}]]))

(defn textarea-field [f o]
  (let [title (titleize (name f))]
    [:div.form-group
     [:label {:for f} title]
     [:textarea.form-control {:id f :name f :rows 10 :placeholder title} (f o)]]))

(defn bytes->string [b]
  (apply str (map char b)))

(defn generate-keypair []
  (let [a (ssh/ssh-agent {})
        keypair-bytes (ssh/generate-keypair a :rsa 2048 "")
        keypair (map bytes->string keypair-bytes)]
    {:private_key (first keypair) :public_key (last keypair)}))

(defn add-keypair-page []
  (let [keypair (generate-keypair)]
    (layout/common
      [:form {:method "post" :action "/keypairs"}
       (text-field :name {})
       (textarea-field :private_key keypair)
       (textarea-field :public_key keypair)
       [:button.btn.btn-default {:type "submit"}
        [:span.glyphicon.glyphicon-ok] " Save"]
       [:a.btn.btn-default {:href "/keypairs"}
        [:span.glyphicon.glyphicon-remove " Cancel"]]])))

(defn edit-keypair-page [id]
  (let [keypair (db/get-keypair id)]
    (layout/common
     [:form {:method "post" :action (str "/keypairs/" id)}
      [:input {:type "hidden" :name "_method" :value "put"}]
      (text-field :name keypair)
      (textarea-field :private_key keypair)
      (textarea-field :public_key keypair)
      [:button.btn.btn-default {:type "submit"}
       [:span.glyphicon.glyphicon-ok] " Save"]
      [:a.btn.btn-default {:href "/keypairs"}
       [:span.glyphicon.glyphicon-remove " Cancel"]]])))

(defn save-keypair [params]
  (let [keypair (select-keys params [:name :private_key :public_key])]
    (db/save-keypair keypair)
    (response/redirect "/keypairs")))

(defn update-keypair [id params]
  (let [keypair (select-keys params [:name :private_key :public_key])]
    (db/update-keypair id keypair)
    (response/redirect "/keypairs")))

(defn view-keypair [id]
  (let [keypair (db/get-keypair id)]
    (layout/common
      [:h1 (:name keypair)]
      [:strong "Private Key"]
      [:pre (:private_key keypair)]
      [:strong "Public Key"]
      [:pre (:public_key keypair)])))

(defn delete-keypair [id]
  (db/delete-keypair id)
  (response/redirect "/keypairs"))

(defroutes keypairs-routes
  (context "/keypairs" []
           (GET "/" [] (list-keypairs-page))
           (GET "/new" [] (add-keypair-page))
           (POST "/" [& params] (save-keypair params))
           (GET "/:id" [id] (view-keypair id))
           (GET "/:id/edit" [id] (edit-keypair-page id))
           (PUT "/:id" [id & params] (update-keypair id params))
           (DELETE "/:id" [id] (delete-keypair id))))
