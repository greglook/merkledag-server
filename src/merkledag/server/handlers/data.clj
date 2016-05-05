(ns merkledag.server.handlers.data
  "Ring handlers for merkledag node operations."
  (:require
    [merkledag.core :as merkle]
    [merkledag.refs :as refs]
    [merkledag.server.handlers.response :refer :all]
    [merkledag.server.routes :refer [data-url]]
    [multihash.core :as multihash]
    [ring.util.response :as r]))


(defn resolve-identifier
  "Resolves a string as either a valid multihash or a ref name."
  [repo ident]
  (try
    (multihash/decode ident)
    (catch Exception _
      (let [ref-val (refs/get-ref (:refs repo) ident)]
        (:value ref-val)))))


(defn handle-create!
  "Handles a request to create a new node from structured data."
  [repo request]
  (try-request
    [has-content? (pos? (:content-length request))]
    (error-response 411 :no-content "Cannot store block with no content")

    (let [{:keys [links data]} (:body-params request)
          node (merkle/create-node! repo links data)]
      (r/redirect (str (data-url (:id node))) :see-other))))


(defn handle-get
  "Handles a request to retrieve node content."
  [repo request]
  (try-request
    [route-id (:ident (:route-params request))]
    (bad-request "No root id provided")

    [root-id (resolve-identifier repo route-id)]
    (bad-request (str "Root identifier is not a valid multihash or ref name: " route-id))

    [node (merkle/get-path repo root-id (:path (:route-params request)))]
    (not-found (str "Path " (:uri request) " not found in repository"))

    (r/response (select-keys node [:id :size :encoding :links :data]))))


(defn handle-put!
  "Handles a request to update node content."
  [repo request]
  (try-request
    [has-content? (pos? (:content-length request))]
    (error-response 411 :no-content "Cannot store block with no content")

    [root-id (:ident (:route-params request))]
    (bad-request "No root id provided")

    [ref-val (:value (refs/get-ref (:refs repo) root-id))]
    (not-found (str "Ref " root-id " not found in tracker (" ex ")"))

    (throw (ex-data "Not Yet Implemented" {}))))



;; ## Handler Constructor

(defn data-handlers
  "Returns a map of node route keys to method maps from http verbs to actual
  request handlers."
  [repo]
  {:data/index
   {:post (partial handle-create! repo)}

   :data/resource
   {:get (partial handle-get repo)
    :put (partial handle-put! repo)}})
