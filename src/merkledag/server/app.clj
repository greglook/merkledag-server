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



;; ## Handler Constructors

(defn route-handler
  "Constructs a new Ring handler which routes the request using bidi.

  The routed keyword is looked up in `handlers`, which should return a map of
  method keywords to handler functions. If no value is found for the handler
  key, a 404 response is returned. If the method map does not contain a value
  for the method (or for `:any`), a 405 response is returned.

  Otherwise, the request is passed along to the located handler function with
  any route parameters added to `:route-params`.

  As a simple example:

  ```
  (route-handler
    [\"/\" :sys/index]
    {:sys/index {:get views/index}})
  ```
  "
  [routes handlers]
  (fn handler
    [request]
    (let [route (bidi/match-route routes (:uri request))]
      (if-let [method-map (and route (get handlers (:handler route)))]
        (if-let [handler (or (get method-map (:request-method request))
                             (:any method-map))]
          (handler (assoc request :route-params (:route-params route)))
          (method-not-allowed (keys method-map)))
        (not-found "No such resource")))))


(defn ring-handler
  "Constructs a new Ring handler implementing the application."
  [repo root-url]
  (route-handler
    routes/routes
    (merge (block-handlers (str root-url "/blocks/") (:store repo))
           (node-handlers (:store repo)))))
