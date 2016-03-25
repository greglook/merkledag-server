(ns merkledag.server.routes
  "Route definition for the application."
  (:require
    [bidi.bidi :as bidi]
    [clojure.string :as str]
    [multihash.core :as multihash]))


(def sys-routes
  ["/sys"
   {"/info" :sys/info
    "/ping" :sys/ping}])


(def block-routes
  ["/blocks"
   {"/" :block/index
    ["/" :id] :block/resource}])


(def ref-routes
  ["/refs"
   {"/" :ref/index
    ["/" :name] :ref/resource
    ["/" :name "/log"] :ref/log}])


(def data-routes
  ["/data"
   {"/" :data/index
    ["/" :id-or-ref] :data/resource
    ["/" :id-or-ref "/" [#".*" :path]] :data/resource}])


(def routes
  ["" [["/" :sys/index]
       sys-routes
       block-routes
       ref-routes
       data-routes]])



;; ## Path Helpers

(defn block-path
  ([]
   (bidi/path-for routes :block/index))
  ([id]
   (bidi/path-for routes :block/resource
                  :id (multihash/base58 id))))


(defn ref-path
  ([]
   (bidi/path-for routes :ref/index))
  ([name]
   (bidi/path-for routes :ref/resource
                  :name name)))


(defn data-path
  ([]
   (bidi/path-for routes :data/index))
  ([id]
   (bidi/path-for routes :data/resource
                  :id (multihash/base58 id)))
  ([id & path]
   (bidi/path-for routes :data/resource
                  :id (multihash/base58 id)
                  :path (str/join "/" path))))
