(ns merkledag.server.core
  "System configuration and setup."
  (:require
    [clojure.tools.logging :as log]
    [com.stuartsierra.component :as component]))


(def system
  "Currently-running server system."
  nil)


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
