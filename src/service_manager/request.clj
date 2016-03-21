(ns service-manager.request)

(def ^:dynamic *request* nil)

(defn wrap-request-binding [handler]
  (fn [request]
    (binding [*request* request]
      (handler request))))
