(ns merkledag.server.jetty
  "This namespace provides a Jetty component for serving the app."
  (:require
    [clojure.tools.logging :as log]
    [com.stuartsierra.component :as component]
    (merkledag.server
      [app :as app]
      [middleware :refer :all])
    [multihash.core :as multihash]
    [ring.adapter.jetty :as jetty]
    (ring.middleware
      [cors :refer [wrap-cors]]
      [format :refer [wrap-restful-format]]
      [keyword-params :refer [wrap-keyword-params]]
      [params :refer [wrap-params]]))
  (:import
    merkledag.link.MerkleLink
    multihash.core.Multihash
    org.eclipse.jetty.server.Server
    org.joda.time.DateTime))


(defmethod print-method Multihash
  [value writer]
  (print-method (tagged-literal 'data/hash (multihash/base58 value)) writer))


(defmethod print-method MerkleLink
  [value writer]
  (print-method (tagged-literal 'data/link ((juxt :target :name :tsize) value)) writer))


(defmethod print-method DateTime
  [value writer]
  (print-method (tagged-literal 'inst (str value)) writer))


(defn- wrap-middleware
  "Wraps the application handler in common middleware."
  [handler]
  (-> handler
      (wrap-cors :access-control-allow-origin [#".*"]
                 :access-control-allow-headers [:content-type]
                 :access-control-allow-methods [:get :put :post :delete])
      (wrap-request-logger 'merkledag.server.handler)
      (wrap-keyword-params)
      (wrap-params)
      (wrap-exception-handler)
      (wrap-restful-format :formats [:edn]) ; TODO: replace with merkledag edn codec
      (wrap-x-forwarded-for)))



;; ## Web Server Component

(defrecord JettyServer
  [root-url options repo ^Server server]

  component/Lifecycle

  (start
    [this]
    (if server
      (do
        (when-not (.isStarted server)
          (log/info "Restarting JettyServer...")
          (.start server))
        this)
      (let [handler (wrap-middleware (app/ring-handler repo root-url))
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
  [root-url & {:as options}]
  (JettyServer. root-url options nil nil))
