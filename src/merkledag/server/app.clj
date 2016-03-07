(ns merkledag.server.app
  "This namespace defines a Ring handler binding URL paths to data lookup and
  controller actions."
  (:require
    [bidi.bidi :as bidi]
    [cemerick.url :as url]
    (merkledag.server
      [routes :as routes])
    (merkledag.server.handlers
      [blocks :refer [block-handlers]]
      [nodes :refer [node-handlers]]
      [response :refer :all])))


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
    ; TODO: find better way to pass route constructors into handlers
    (merge (block-handlers (str root-url "/blocks/") (:store repo))
           (node-handlers (str root-url "/nodes/") (:store repo)))))
