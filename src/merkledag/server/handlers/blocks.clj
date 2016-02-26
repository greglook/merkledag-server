(ns merkledag.server.handlers.blocks
  "Ring handlers for raw block operations."
  (:require
    [blocks.core :as block]
    [cemerick.url :as url]
    [clojure.string :as str]
    [merkledag.server.response :refer :all]
    [multihash.core :as multihash]
    [ring.util.response :as r]))


(defn block-headers
  "Constructs a map of HTTP headers from a block's metadata."
  [block]
  (let [stored-at (or (:stored-at block)
                      (:stored-at (block/meta-stats block)))]
    (cond->
      {"Content-Type" "application/octet-stream"
       "Content-Length" (str (:size block))}
      stored-at
        (assoc "Last-Modified" (format-date stored-at)))))



;; ## Collection Handlers

(defn handle-list
  "Handles a request to list stats about the stored blocks."
  [store base-url request]
  (let [{:keys [after limit]} (:params request)
        max-limit 100
        limit (if limit (min (Integer/parseInt limit) max-limit) max-limit)
        stats (block/list store :after after :limit limit)]
    (-> (r/response {:items (mapv #(assoc :href (str base-url (multihash/base58 (:id %)))) stats)})
        (cond->
          (<= limit (count stats))
            (r/header "Link" (->> {:next (assoc base-url :query {:after (multihash/base58 (:id (last stats)))
                                                                 :limit limit})}
                                  (map #(format "<%s>; rel=\"%s\"" (val %) (name (key %))))
                                  (str/join ",")))))))


(defn handle-store!
  "Handles a request to store a new block."
  [store base-url request]
  (let [size (:content-length request)]
    (if (and size (pos? size))
      (let [block (block/store! store (:body request))
            location (str base-url (multihash/base58 (:id block)))]
        (-> (r/response
              {:id (:id block)
               :size (:size block)
               :stored-at (:stored-at (block/meta-stats block))
               :href location})
            (r/status 201)
            (r/header "Location" location)))
      (error-response 411 :no-content "Cannot store block with no content"))))



;; ## Block Handlers

(defn handle-stat
  "Handles a request to look up metadata about a stored bolck."
  [store request]
  (if-let [id (:id (:route-params request))]
    (if-let [stats (block/stat store id)]
      (-> (r/response "")
          (assoc :headers (block-headers stats)))
      (not-found (str "Block " id " not found in store")))
    (bad-request "No block id provided")))


(defn handle-get
  "Handles a request to retrieve raw block content."
  [store request]
  (if-let [id (:id (:route-params request))]
    (if-let [block (block/get store (multihash/decode id))]
      ; TODO: support ranged-open (Accept-Ranges: bytes / Content-Range: bytes 21010-47021/47022)
      (-> (r/response (block/open block))
          (assoc :headers (block-headers block)))
      (not-found (str "Block " id " not found in store")))
    (bad-request "No block id provided")))


(defn handle-delete!
  "Handles a request to delete a block."
  [store request]
  (if-let [id (:id (:route-params request))]
    (if (block/delete! store (multihash/decode id))
      (-> (r/response "")
          (r/status 204))
      (not-found (str "Block " id " not found in store")))
    (bad-request "No block id provided")))



;; ## Handler Constructor

(defn block-handlers
  "Returns a map of block route keys to method maps from http verbs to actual
  request handlers."
  [store base-url]
  (let [base-url (url/url base-url)]
    {:block/index
     {:get  (partial handle-list store base-url)
      :post (partial handle-store! store base-url)}

     :block/resource
     {:head   (partial handle-stat store)
      :get    (partial handle-get store)
      :delete (partial handle-delete! store)}}))
