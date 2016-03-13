(ns service-manager.views.status)

(defn success-status [message]
  [:span.text-success [:span.glyphicon.glyphicon-ok-sign] " " message])

(defn warning-status [message]
  [:span.text-warning [:span.glyphicon.glyphicon-question-sign] " " message])

(defn error-status [message]
  [:span.text-danger [:span.glyphicon.glyphicon-remove-sign] " " message])

(defn host-status [element host-id]
  [element
   {:class "host-status" :data-host-id host-id}
   (warning-status "Checking...")])

(defn host-service-status [element host-id service-id]
  [element
   {:class "host-service-status" :data-host-id host-id :data-service-id service-id}
   (warning-status "Checking...")])

(defn ssh-cmd-result-status [result]
  (if (= 0 (:exit result))
    (success-status (:out result))
    (error-status (str (:out result) (:err result)))))

(defn ssh-exception-status [e]
  (error-status
    (if-let [cause (.getCause e)]
      (condp instance? cause
        java.net.UnknownHostException "Unknown host"
        java.net.NoRouteToHostException "No route to host"
        (.getMessage cause))
      (.getMessage e))))

(defn multi-status [& statuses]
  (interpose [:br] (distinct statuses)))
