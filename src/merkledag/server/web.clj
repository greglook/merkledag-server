(ns merkledag.server.web
  "This namespace provides a Jetty component for serving the app."
  (:require
    [clojure.tools.logging :as log]
    [com.stuartsierra.component :as component]
    (merkledag.server
      [app :as app]
      [middleware :refer :all])
    [ring.adapter.jetty :as jetty]
    (ring.middleware
      [content-type :refer [wrap-content-type]]
      [format :refer [wrap-restful-format]]
      [keyword-params :refer [wrap-keyword-params]]
      [not-modified :refer [wrap-not-modified]]
      [params :refer [wrap-params]]
      [resource :refer [wrap-resource]]))
  (:import
    org.eclipse.jetty.server.Server))


(defn- wrap-middleware
  "Wraps the application handler in common middleware."
  [handler]
  (-> handler
      (wrap-request-logger 'merkledag.server.handler)
      (wrap-keyword-params)
      (wrap-params)
      (wrap-resource "public")
      (wrap-content-type)
      (wrap-cache-control #{"text/css" "text/javascript"} :max-age 300)
      (wrap-not-modified)
      (wrap-exception-handler)
      (wrap-restful-format :formats [:json :edn])
      (wrap-x-forwarded-for)))



;; ## Web Server Component

(defrecord JettyServer
  [options ^Server server]

  component/Lifecycle

  (start
    [this]
    (if server
      (do
        (when-not (.isStarted server)
          (log/info "Restarting JettyServer...")
          (.start server))
        this)
      (let [handler (wrap-middleware app/ring-handler)
            options (assoc options :join? false)]
        (log/info (str "Starting JettyServer on port " (:port options) "..."))
        (assoc this :server (jetty/run-jetty handler options)))))


  (stop
    [this]
    (when (and server (not (.isStopped server)))
      (log/info "Stopping JettyServer...")
      (.stop server))
    this))


(defn jetty-server
  "Constructs a new web server component with the given options."
  [& {:as options}]
  (JettyServer. options nil))
