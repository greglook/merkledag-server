(ns merkledag.server.handlers.data
  "Ring handlers for merkledag node operations."
  (:require
    [merkledag.core :as merkle]
    [merkledag.server.handlers.response :refer :all]
    [merkledag.server.routes :refer [data-url]]
    [multihash.core :as multihash]
    [ring.util.response :as r]))


(defn handle-create!
  "Handles a request to create a new node from structured data."
  [repo request]
  (try-request
    [has-content? (pos? (:content-length request))]
    (error-response 411 :no-content "Cannot store block with no content")

    (let [node (merkle/put-node! repo (:links (:body request)) (:data (:body request)))]
      (r/redirect (data-url (:id node)) :see-other))))


(defn handle-get
  "Handles a request to retrieve node content."
  [repo request]
  (try-request
    [id (:id (:route-params request))]
    (bad-request "No block id provided")

    [id (multihash/decode id)]
    (bad-request (str "Error parsing multihash: " ex))

    [node (merkle/get-node repo id)]
    (if ex
      (throw ex)
      (not-found (str "Node " id " not found in Repository")))

    ; TODO: something with :path?

    (r/response (select-keys node [:id :size :encoding :links :data]))))



;; ## Handler Constructor

(defn node-handlers
  "Returns a map of node route keys to method maps from http verbs to actual
  request handlers."
  [repo]
  {:node/index
   {:post (partial handle-create! repo)}

   :node/resource
   {:get (partial handle-get repo)}})
