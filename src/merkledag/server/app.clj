(ns merkledag.server.app
  "This namespace defines a Ring handler binding URL paths to data lookup and
  controller actions."
  (:require
    [bidi.bidi :as bidi]
    (merkledag.server
      [response :refer :all]
      [routes :as route])
    (merkledag.server.handlers
      [blocks :refer [block-handlers]])))


(def server-root "http://localhost:8080")


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
  [repo]
  (let [handlers (merge (block-handlers (:store repo) (str server-root "/blocks/"))
                        (node-handlers (:store repo)))]
    (fn handler
      [request]
      (let [route (bidi/match-route route/routes (:uri request))]
        (if-let [method-map (and route (get handlers (:handler route)))]
          (if-let [handler (or (get method-map (:request-method request))
                               (:any method-map))]
            (handler (assoc request :route-params (:route-params route)))
            (method-not-allowed (keys method-map)))
          (not-found "No such resource"))))))
