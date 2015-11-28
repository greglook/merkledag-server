(ns merkledag.server.app
  "This namespace defines a Ring handler binding URL paths to data lookup and
  controller actions."
  (:require
    [bidi.bidi :as bidi]
    [clojure.tools.logging :as log]
    (merkledag.server
      [response :refer :all]
      [views :as views])
    [ring.util.response :as r]))


(def routes
  ["/" [["" :sys/index]
        ["blocks/" {"" :block/index
                    [:id] :block/resource}]
        ["nodes/" {"" :node/index
                   [:id] :node/resource
                   [:id "/links"] :node/links
                   [:id "/data"] :node/data}]]])


(defn- nyi
  [message]
  (fn handler
    [request]
    (-> (r/response {:error :not-implemented
                     :message (str message " is not yet implemented")})
        (r/status 500))))


(def resource-handlers
  {:sys/index
   {:get (fn [r] (render (views/index)))}

   :block/index
   {:get  (nyi "list-blocks")
    :post (nyi "store-block")}

   :block/resource
   {:head   (nyi "stat-block")
    :get    (nyi "get-block")
    :delete (nyi "delete-block")}

   :node/index
   {:post (nyi "create-node")}

   :node/resource
   {:get (nyi "get-node")}

   :node/links
   {:get (nyi "get-node-links")}

   :node/data
   {:get (nyi "get-node-data")}})


(defn ring-handler
  "Constructs a new Ring handler implementing the application."
  [request]
  (if-let [route (bidi/match-route routes (:uri request))]
    (if-let [method-map (get resource-handlers (:handler route))]
      (if-let [handler (or (get method-map (:request-method request))
                           (:any method-map))]
        (handler (assoc request :route-params (:route-params route)))
        (method-not-allowed (keys method-map)))
      (not-found "No such resource"))
    (not-found "No such resource")))
