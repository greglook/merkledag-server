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
      [resource :refer [wrap-resource]]
      [session :refer [wrap-session]])
    [ring.middleware.session.cookie :as cookie])
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


(defn- service-handler
  "Constructs the fully-configured service handler."
  [controller session-key]
  (-> (app/ring-handler controller)
      (wrap-session
        {:store (cookie/cookie-store {:key session-key})})
      (wrap-middleware)))



;; ## Web Server Component

(defrecord JettyServer
  [options session-key controller ^Server server]

  component/Lifecycle

  (start
    [this]
    (if server
      (do
        (if-not (.isStarted server)
          (do
            (log/info "Restarting JettyServer...")
            (.start server))
          (log/info "JettyServer is already started"))
        this)
      (let [handler (service-handler controller session-key)
            options (assoc options :join? false)]
        (log/info (str "Starting JettyServer on port " (:port options) "..."))
        (assoc this :server (jetty/run-jetty handler options)))))


  (stop
    [this]
    (log/info "Stopping JettyServer...")
    (when (and server (not (.isStopped server)))
      (.stop server))
    this))


(defn jetty-server
  "Constructs a new web server component with the given options."
  [& {:as options}]
  (map->JettyServer
    {:options (dissoc options :session-key)
     :session-key (:session-key options)}))
