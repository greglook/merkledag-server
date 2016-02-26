(ns merkledag.server.app
  "This namespace defines a Ring handler binding URL paths to data lookup and
  controller actions."
  (:require
    (merkledag.server
      [response :refer :all]
      [routes :as route])
    (merkledag.server.handlers
      [blocks :refer [block-handlers]])))


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
