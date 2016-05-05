(ns merkledag.server.core
  "Main system configuration and setup. The component system is configured
  primarily using environment variables:

  ### General Settings

  - `port` TCP port to serve the web application on.
  - `server-url` Base location of the server. Defaults to localhost and the configured port.
  "
  (:gen-class)
  (:require
    (blocks.store
      [cache :refer [cache-store]]
      [file :refer [file-store]]
      [memory :refer [memory-store]])
    [clojure.tools.logging :as log]
    [com.stuartsierra.component :as component]
    [environ.core :refer [env]]
    [merkledag.core :as merkle]
    [merkledag.refs.file :refer [file-tracker]]
    [merkledag.server.jetty :as jetty]))


(defn create-system
  []
  (let [port (Integer/parseInt (env :port "8080"))
        server-url (env :server-url (str "http://localhost:" port))]
    (component/system-map
      :file-store  ; TODO: make this more configurable
      (file-store (env :store-root "dev-repo/blocks"))

      :memory-store
      (memory-store)

      :store
      (component/using
        (let [mb (partial * 1024 1024)]
          (cache-store (mb 512)
            :max-block-size (mb 8)))
        {:primary :file-store
         :cache :memory-store})

      :refs
      (file-tracker (env :tracker-file "dev-repo/refs.tsv"))

      :repo
      (component/using
        (merkle/graph-repo)
        [:store :refs])

      :server
      (component/using
        (jetty/jetty-server
          :root-url server-url
          :server (env :bind-addr "127.0.0.1")
          :port port
          :min-threads 2
          :max-threads 5
          :max-queued 25)
        [:repo]))))


(def system
  "Currently-running server system."
  nil)


(defn init!
  "Initialize the system from environment variables."
  []
  (alter-var-root #'system (constantly (create-system)))
  :init)


(defn start!
  "Runs the initialized system."
  []
  (when-not system
    (throw (IllegalStateException. "Cannot start uninitialized system")))
  (log/info "Starting component system...")
  (alter-var-root #'system component/start)
  ; TODO: extend component lifecycle to file tracker instead of calling this here.
  (merkledag.refs.file/load-history! (:refs system))
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
