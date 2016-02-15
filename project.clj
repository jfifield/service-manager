(defproject service-manager "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [compojure "1.1.6"]
                 [hiccup "1.0.5"]
                 [ring-server "0.3.1"]
                 [ring-webjars "0.1.1"]
                 [org.webjars/bootstrap "3.3.6"]
                 [org.webjars/jquery "1.11.3"]]
  :plugins [[lein-ring "0.8.12"]]
  :ring {:handler service-manager.handler/app
         :init service-manager.handler/init
         :destroy service-manager.handler/destroy}
  :profiles
  {:uberjar {:aot :all}
   :production
   {:ring
    {:open-browser? false, :stacktraces? false, :auto-reload? false}}
   :dev
   {:dependencies [[ring-mock "0.1.5"] [ring/ring-devel "1.3.1"]]}})
