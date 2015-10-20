(ns merkledag.server.main
  "Main entry-point for launching the server standalone. The component system
  is configured primarily using environment variables:

  ### General Settings

  - `port` TCP port to serve the web application on.
  - `server-url` Base location of the server. Defaults to localhost and the configured port.
  "
  (:gen-class)
  (:require
    [clojure.tools.logging :as log]
    [com.stuartsierra.component :as component]
    [environ.core :refer [env]]
    (merkledag.server
      [core :as core]
      [web :as web])))


(defn init!
  "Initialize the system from environment variables."
  []
  (alter-var-root #'core/system
    (constantly
      (let [port (Integer/parseInt (env :port "8080"))
            server-url (env :server-url (str "http://localhost:" port))]
         (component/system-map
           :repo
           {:url (env :repo-url "memory://")}

           :controller
           (component/using
             {}
             [:repo])

           :web
           (component/using
             (web/jetty-server
               :server (env :bind-addr "127.0.0.1")
               :port port
               :min-threads 2
               :max-threads 5
               :max-queued 25
               :session-key (env :session-key))
             [:controller])))))
  :init)


(defn -main []
  (init!)
  (.addShutdownHook
    (Runtime/getRuntime)
    (Thread. ^Runnable core/stop! "System Shutdown Hook"))
  (core/start!)
  (log/info "Server started, entering active mode..."))
