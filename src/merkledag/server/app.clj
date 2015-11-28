(ns merkledag.server.app
  "This namespace defines a Ring handler binding URL paths to data lookup and
  controller actions."
  (:require
    [bidi.bidi :as bidi]
    [blocks.core :as block]
    [cemerick.url :as url]
    [cheshire.generate :as chgen]
    [clojure.tools.logging :as log]
    [merkledag.core :as merkle]
    (merkledag.server
      [response :refer :all]
      [routes :as route]
      [views :as views])
    [multihash.core :as multihash]
    [ring.util.response :as r]))


(chgen/add-encoder java.net.URI chgen/encode-str)
(chgen/add-encoder multihash.core.Multihash chgen/encode-str)

(def server-root "http://localhost:8080")


(defn- nyi
  [message]
  (fn handler
    [request]
    (-> (r/response {:error :not-implemented
                     :message (str message " is not yet implemented")})
        (r/status 500))))


(defn list-blocks
  [store request]
  (let [{:keys [after limit]} (:params request)
        limit (if limit (min (Integer/parseInt limit) 100) 100)
        stats (block/list store :after after :limit limit)]
    (r/response {:entries (mapv
                            #(assoc % :href (str (url/url server-root (route/path-for-node (:id %)))))
                            stats)})))


(defn get-block
  [store request]
  (if-let [id (:id (:route-params request))]
    (let [mhash (multihash/decode id)
          block (block/get store mhash)]
      (if block
        (r/response (block/open block))
        (not-found "No such block" :id mhash)))
    (bad-request "No block id provided")))


(defn get-node
  [repo request]
  (if-let [id (:id (:route-params request))]
    (let [mhash (multihash/decode id)
          node (merkle/get-node repo mhash)]
      (if node
        (render (views/show-node (get-in repo [:format :codec :codecs :edn :types]) node))
        (not-found "No such block" :id mhash)))
    (bad-request "No block id provided")))



(def sys-handlers
  {:sys/index
   {:get (fn [r] (render (views/index)))}})


(defn block-handlers
  [store]
  {:block/index
   {:get  (partial list-blocks store)
    :post (nyi "store-block")}

   :block/resource
   {:head   (nyi "stat-block")
    :get    (partial get-block store)
    :delete (nyi "delete-block")}})


(defn node-handlers
  [repo]
  {:node/index
   {:post (nyi "create-node")}

   :node/resource
   {:get (partial get-node repo)}

   :node/links
   {:get (nyi "get-node-links")}

   :node/data
   {:get (nyi "get-node-data")}})


(defn ring-handler
  "Constructs a new Ring handler implementing the application."
  [repo]
  (let [handlers (merge sys-handlers
                        (block-handlers repo)
                        (node-handlers repo))]
    (fn handler
      [request]
      (let [route (bidi/match-route route/routes (:uri request))]
        (if-let [method-map (and route (get handlers (:handler route)))]
          (if-let [handler (or (get method-map (:request-method request))
                               (:any method-map))]
            (handler (assoc request :route-params (:route-params route)))
            (method-not-allowed (keys method-map)))
          (not-found "No such resource"))))))
