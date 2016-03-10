(ns merkledag.server.routes
  "Route definition for the application."
  (:require
    [bidi.bidi :as bidi]
    [multihash.core :as multihash]))


(def routes
  ["" [["/" :sys/index]
       ["/sys"
        {"/info" :sys/info
         "/ping" :sys/ping}]
       ["/blocks"
        {"/" :block/index
         ["/" :id] :block/resource}]
       ["/nodes"
        {"/" :node/index
         ["/" :id] :node/resource
         ["/" :id "/" [#".*" :path]] :node/resource}]
       ["/refs"
        {"/" :refs/index
         ["/" :name] :refs/resource}]]])



;; ## Path Helpers

(defn block-path
  [id]
  (bidi/path-for routes :block/resource :id (multihash/base58 id)))


(defn node-path
  [id]
  (bidi/path-for routes :node/resource :id (multihash/base58 id)))
