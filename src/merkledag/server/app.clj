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
    [merkledag.server.middleware :refer :all]
    [merkledag.server.routes :as route]
    [multihash.core :as multihash]
    (ring.middleware
      [cors :refer [wrap-cors]]
      [format :refer [wrap-restful-format]]
      [keyword-params :refer [wrap-keyword-params]]
      [params :refer [wrap-params]]))
  (:import
    merkledag.link.MerkleLink
    multihash.core.Multihash
    org.joda.time.DateTime))


;; ## Print Methods

(defmethod print-method Multihash
  [value writer]
  (print-method (tagged-literal 'data/hash (multihash/base58 value)) writer))


(defmethod print-method MerkleLink
  [value writer]
  (print-method (tagged-literal 'data/link ((juxt :target :name :tsize) value)) writer))


(defmethod print-method DateTime
  [value writer]
  (print-method (tagged-literal 'inst (str value)) writer))



;; ## Route Handler

; TODO: move this to a common lib
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


(defn app-handler
  "Constructs a new Ring handler implementing the application."
  [repo]
  (route-handler
    route/routes
    (merge (block-handlers (:store repo))
           (ref-handlers (:refs repo))
           (data-handlers repo)
           (sys-handlers))))


(defn service-handler
  "Wraps the application handler in common middleware."
  [root-url repo]
  (-> (app-handler repo)
      (route/wrap-url-context root-url)
      (wrap-cors :access-control-allow-origin [#".*"]
                 :access-control-allow-headers [:content-type]
                 :access-control-allow-methods [:get :put :post :delete])
      (wrap-request-logger "merkledag.server.app")
      (wrap-keyword-params)
      (wrap-params)
      (wrap-exception-handler)
      (wrap-restful-format :formats [:edn]) ; TODO: replace with merkledag edn codec
      (wrap-x-forwarded-for)))
