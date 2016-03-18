(ns merkledag.server.handlers.sys
  "Ring handlers for system info."
  (:require
    [cemerick.url :as url]
    [clojure.string :as str]
    [merkledag.server.handlers.response :refer :all]
    [ring.util.response :as r]))


(defn handle-index
  "Handles a request for the service root path."
  [request]
  ; ...
  )


(defn handle-info
  "Handles a request for service info."
  [request]
  ; ...
  )


(defn handle-ping
  "Handles a ping request to health-check the service."
  [request]
  ; TODO: more sophisticated health-check
  (r/response "pong"))



;; ## Handler Constructor

(defn sys-handlers
  "Returns a map of sys route keys to method maps from http verbs to actual
  request handlers."
  []
  {:sys/index {:get handle-index}
   :sys/info  {:get handle-info}
   :sys/ping  {:get handle-ping}})
