(ns merkledag.server.handlers.refs
  "Ring handlers for ref operations."
  (:require
    [cemerick.url :as url]
    [clojure.string :as str]
    [merkledag.refs :as refs]
    [merkledag.server.handlers.response :refer :all]
    [multihash.core :as multihash]
    [ring.util.response :as r]))


(defn ref-url
  [base ref]
  (str (url/url base (:name ref))))


(defn handle-list
  "Handles a request to list references stored in a tracker."
  [tracker base-url request]
  (let [{:keys [include-nil limit]} (:params request)
        max-limit 100
        limit (if limit (min (Integer/parseInt limit) max-limit) max-limit)
        refs (refs/list-refs tracker {:include-nil include-nil, :limit limit})]
    (r/response {:items (mapv #(-> %
                                   (select-keys [:name :value :version :time])
                                   (assoc :href (ref-url base-url %)))
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
  (try-request
    [rname (:name (:route-params request))]
    (bad-request "Missing ref name in route-params")

    [history (refs/list-ref-history tracker rname)]
    (not-found (str "Ref " rname " not found in tracker"))

    (r/response {:items (vec history)})))


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
  (try-request
    [rname (:name (:route-params request))]
    (bad-request "Missing ref name in route-params")

    (if (refs/delete-ref! tracker rname)
      (-> (r/response "") (r/status 204))
      (not-found (str "Ref " rname " not found in tracker")))))



;; ## Handler Constructor

(defn ref-handlers
  "Returns a map of ref route keys to method maps from http verbs to actual
  request handlers."
  [base-url tracker]
  {:ref/index
   {:get (partial handle-list tracker base-url)}

   :ref/resource
   {:get    (partial handle-get tracker)
    :put    (partial handle-set! tracker)
    :delete (partial handle-delete! tracker)}

   :ref/log
   {:get (partial handle-log tracker)}})
