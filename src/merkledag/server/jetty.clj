(ns merkledag.server.jetty
  "This namespace provides a Jetty component for serving the app."
  (:require
    [clojure.tools.logging :as log]
    [com.stuartsierra.component :as component]
    [merkledag.server.app :as app]
    [ring.adapter.jetty :as jetty])
  (:import
    org.eclipse.jetty.server.Server))


(defrecord JettyServer
  [options repo ^Server server]

  component/Lifecycle

  (start
    [this]
    (if server
      (do
        (when-not (.isStarted server)
          (log/info "Restarting JettyServer...")
          (.start server))
        this)
      (let [handler (app/service-handler (:root-url options) repo)
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
  (JettyServer. options nil nil))
