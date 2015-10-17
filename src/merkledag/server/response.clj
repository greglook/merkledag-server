(ns merkledag.server.response
  "This namespace provides some simple helper functions for generating Ring
  response maps."
  (:require
    [clojure.string :as str]
    [hiccup.core :as hiccup]
    [ring.util.response :as r]))


(defn bad-request
  [message]
  (-> (r/response {:error :bad-request, :message message})
      (r/status 400)))


(defn not-found
  [message]
  (-> (r/response {:error :not-found, :message message})
      (r/status 404)))


(defn method-not-allowed
  [& allowed]
  (-> (r/response {:error :not-allowed, :message "Method not allowed on this resource"})
      (r/header "Allow" (str/join ", " (map (comp str/upper-case name) allowed)))
      (r/status 405)))


(defn render
  "Renders a Hiccup template data structure to HTML and returns a response with
  the correct content type."
  [template]
  (-> (hiccup/html template)
      (r/response)
      (r/content-type "text/html")
      (r/charset "utf-8")))
