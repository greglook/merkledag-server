(ns merkledag.server.app
  "This namespace defines a Ring handler binding URL paths to data lookup and
  controller actions."
  (:require
    [bidi.bidi :as bidi]
    [cemerick.url :as url]
    (merkledag.server
      [response :refer :all]
      [routes :as routes])
    (merkledag.server.handlers
      [blocks :as bh])))


;; ## Handler Maps

(defn block-handlers
  "Returns a map of block route keys to method maps from http verbs to actual
  request handlers."
  [base-url store]
  {:block/index
   {:get  (partial bh/handle-list store base-url)
    :post (partial bh/handle-store! store base-url)}

   :block/resource
   {:head   (partial bh/handle-stat store)
    :get    (partial bh/handle-get store)
    :delete (partial bh/handle-delete! store)}})


(defn node-handlers
  [repo]
  {:node/index
   {:post (nyi "create-node")}

   :node/resource
   {:get (nyi "get-node")}

   :node/links
   {:get (nyi "get-node-links")}

   :node/data
   {:get (nyi "get-node-data")}})


(defn ring-handler
  "Constructs a new Ring handler implementing the application."
  [repo root-url]
  (-> (merge (block-handlers (:store repo) (str root-url "/blocks/"))
             (node-handlers (:store repo)))
      (route/route-handler)))
