(ns merkledag.server.routes
  "Route definition for the application."
  (:require
    [bidi.bidi :as bidi]
    [multihash.core :as multihash]))


(def routes
  ["/" [["" :sys/index]
        ["blocks/" {"" :block/index
                    [:id] :block/resource}]
        ["nodes/" {"" :node/index
                   [:id] :node/resource
                   [:id "/links"] :node/links
                   [:id "/data"] :node/data}]]])


(defn block-path
  [id]
  (bidi/path-for routes :block/resource :id (multihash/base58 id)))


(defn node-path
  [id]
  (bidi/path-for routes :node/resource :id (multihash/base58 id)))
