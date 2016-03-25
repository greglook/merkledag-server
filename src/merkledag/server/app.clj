(ns merkledag.server.app
  "This namespace defines a Ring handler binding URL paths to data lookup and
  controller actions."
  (:require
    [bidi.bidi :as bidi]
    [cemerick.url :as url]
    (merkledag.server.handlers
      [blocks :refer [block-handlers]]
      [data :refer [data-handlers]]
      [refs :refer [ref-handlers]]
      [response :refer :all]
      [sys :refer [sys-handlers]])
    [merkledag.server.routes :as routes]))


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
  (route/wrap-url-context
    (route-handler
      routes/routes
      ; TODO: find better way to pass route constructors into handlers
      (merge (block-handlers (:store repo))
             (ref-handlers (:refs repo))
             (data-handlers repo)
             (sys-handlers)))
    root-url))
