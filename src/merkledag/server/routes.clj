(ns merkledag.server.routes
  "Route definition for the application."
  (:require
    [bidi.bidi :as bidi]
    [merkledag.server.response :refer [method-not-allowed not-found]]
    [multihash.core :as multihash]))


(def routes
  ["/" [["" :sys/index]
        ["blocks/" {"" :block/index
                    [:id] :block/resource}]
        ["nodes/" {"" :node/index
                   ; TODO: path logic?
                   [:id] :node/resource}]]])



;; ## Path Helpers

(defn block-path
  [id]
  (bidi/path-for routes :block/resource :id (multihash/base58 id)))


(defn node-path
  [id]
  (bidi/path-for routes :node/resource :id (multihash/base58 id)))



;; ## Routing Handler

(defn route-handler
  "Constructs a new Ring handler which routes to the given map of handlers."
  [handlers]
  (fn handler
    [request]
    (let [route (bidi/match-route routes (:uri request))]
      (if-let [method-map (and route (get handlers (:handler route)))]
        (if-let [handler (or (get method-map (:request-method request))
                             (:any method-map))]
          (handler (assoc request :route-params (:route-params route)))
          (method-not-allowed (keys method-map)))
        (not-found "No such resource")))))
