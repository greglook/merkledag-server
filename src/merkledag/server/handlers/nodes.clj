(ns merkledag.server.handlers.nodes
  "Ring handlers for merkledag node operations."
  (:require
    [alphabase.base58 :as b58]
    [blocks.core :as block]
    [cemerick.url :as url]
    [clojure.string :as str]
    [merkledag.server.handlers.response :refer :all]
    [multihash.core :as multihash]
    [ring.util.response :as r]))


;; ## Collection Handlers

(defn handle-create!
  "Handles a request to create a new node from structured data."
  [store base-url request]
  (let [size (:content-length request)]
    (if (and size (pos? size))
      ; TODO: parse body as EDN, serialize into node
      (error-response 500 :not-implemented "Not Yet Implemented")
      (error-response 411 :no-content "Cannot store block with no content"))))



;; ## Node Handlers

(defn handle-stat
  "Handles a request to look up metadata about a stored node."
  [store request]
  (try-request
    [id (:id (:route-params request))]
    (bad-request "No block id provided")

    [id (multihash/decode id)]
    (bad-request (str "Error parsing multihash: " ex))

    ; TODO: implement
    (error-response 500 :not-implemented "Not Yet Implemented")))


(defn handle-get
  "Handles a request to retrieve node content."
  [store request]
  (try-request
    [id (:id (:route-params request))]
    (bad-request "No block id provided")

    [id (multihash/decode id)]
    (bad-request (str "Error parsing multihash: " ex))

    ; TODO: something with :path

    ; TODO: implement
    (error-response 500 :not-implemented "Not Yet Implemented")))



;; ## Handler Constructor

(defn node-handlers
  "Returns a map of node route keys to method maps from http verbs to actual
  request handlers."
  [store base-url]
  (let [base-url (url/url base-url)]
    {:node/index
     {:post (partial handle-create! store base-url)}

     :node/resource
     {:head (partial handle-stat store)
      :get  (partial handle-get store)}}))
