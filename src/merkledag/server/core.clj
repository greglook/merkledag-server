(ns merkledag.server.core
  "Main system configuration and setup. The component system is configured
  primarily using environment variables:

  ### General Settings

  - `port` TCP port to serve the web application on.
  - `server-url` Base location of the server. Defaults to localhost and the configured port.
  "
  (:gen-class)
  (:require
    [blocks.store.file :refer [file-store]]
    [clojure.tools.logging :as log]
    [com.stuartsierra.component :as component]
    [environ.core :refer [env]]
    [merkledag.core :as merkle]
    [merkledag.refs.memory :refer [memory-tracker]]
    [merkledag.server.jetty :as jetty]))


(def system
  "Currently-running server system."
  nil)


(defn init!
  "Initialize the system from environment variables."
  []
  (alter-var-root #'system
    (constantly
      (let [port (Integer/parseInt (env :port "8080"))
            server-url (env :server-url (str "http://localhost:" port))]
        (component/system-map
          :store  ; TODO: make this more configurable
          (file-store (env :store-root "dev/blocks"))

          :refs
          (memory-tracker)

          :repo
          (component/using
            (merkle/graph-repo :codec @merkle/block-codec)
            [:store :refs])

          :server
          (component/using
            (jetty/jetty-server
              server-url
              :server (env :bind-addr "127.0.0.1")
              :port port
              :min-threads 2
              :max-threads 5
              :max-queued 25)
            [:repo])))))
  :init)


(defn start!
  "Runs the initialized system."
  []
  (when-not system
    (throw (IllegalStateException. "Cannot start uninitialized system")))
  (log/info "Starting component system...")
  (alter-var-root #'system component/start)
  :start)


(defn stop!
  "Halts the running system."
  []
  (when system
    (log/info "Stopping component system...")
    (alter-var-root #'system component/stop))
  :stop)


(defn -main
  "Stand-alone entry point for running a server."
  []
  (init!)
  (.addShutdownHook
    (Runtime/getRuntime)
    (Thread. ^Runnable stop! "System Shutdown Hook"))
  (start!)
  (log/info "Server started, entering active mode..."))
