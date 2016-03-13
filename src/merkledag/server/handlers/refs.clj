(ns merkledag.server.handlers.refs
  "Ring handlers for ref operations."
  (:require
    [cemerick.url :as url]
    [clojure.string :as str]
    [merkledag.refs :as refs]
    [merkledag.server.handlers.response :refer :all]
    [multihash.core :as multihash]
    [ring.util.response :as r]))


(defn handle-list
  "Handles a request to list references stored in a tracker."
  [tracker base-url request]
  (let [{:keys [include-nil limit]} (:params request)
        max-limit 100
        limit (if limit (min (Integer/parseInt limit) max-limit) max-limit)
        refs (refs/list-refs tracker {:include-nil include-nil, :limit limit})]
    (r/response {:items (mapv #(-> %
                                   (select-keys [:name :value :version :time])
                                   (assoc :href (str (url/url base-url (:name %)))))
                              refs)})))


(defn handle-get
  "Handles a request to retrieve the current version of a ref."
  [tracker request]
  (try-request
    [rname (:name (:route-params request))]
    (bad-request "Missing ref name in route-params")

    [current (if-let [version (:version (:params request))]
               (refs/get-ref tracker rname (Integer/parseInt version))
               (refs/get-ref tracker rname))]
    (not-found (str "Ref " rname " not found in tracker at that version"))

    (r/response current)))


(defn handle-log
  "Handles a request to retrieve the history of versions for a ref."
  [tracker request]
  '...)


(defn handle-set!
  "Handles a request to set the value for a reference."
  [tracker request]
  (try-request
    [rname (:name (:route-params request))]
    (bad-request "Missing ref name in route-params")

    [value (:value (:params request))]
    (bad-request "Missing value parameter")

    [target (multihash/decode value)]
    (bad-request (str "Error parsing multihash: " ex))

    (-> (refs/set-ref! tracker rname target)
        (r/response)
        (r/status 201))))


(defn handle-delete!
  "Handles a request to delete a block."
  [tracker request]
  #_
  (try-request
    [id (:id (:route-params request))]
    (bad-request "No block id provided")

    [id (multihash/decode id)]
    (bad-request (str "Error parsing multihash: " ex))

    (if (block/delete! store (multihash/decode id))
      (-> (r/response "")
          (r/status 204))
      (not-found (str "Block " id " not found in store")))))



;; ## Handler Constructor

(defn ref-handlers
  "Returns a map of ref route keys to method maps from http verbs to actual
  request handlers."
  [base-url tracker]
  {:refs/index
   {:get (partial handle-list tracker base-url)}

   :refs/resource
   {:get    (partial handle-get tracker)
    :put    (partial handle-set! tracker)
    :delete (partial handle-delete! tracker)}

   :refs/log
   {:get (partial handle-log tracker)}})
