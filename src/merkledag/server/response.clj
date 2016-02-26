(ns merkledag.server.response
  "This namespace provides some simple helper functions for generating Ring
  response maps."
  (:require
    [clojure.string :as str]
    [hiccup.core :as hiccup]
    [ring.util.response :as r]))


(defn error-response
  [status error-key message]
  (-> (r/response {:error error-key, :message message})
      (r/status status)))


(defn bad-request
  [message]
  (error-response 400 :bad-request message))


(defn not-found
  [message & {:as extra}]
  (-> (error-response 404 :not-found message)
      (update-in [:body] merge extra)))


(defn method-not-allowed
  [allowed-methods]
  (-> (error-response 405 :not-allowed "Method not allowed on this resource")
      (r/header "Allow" (str/join ", " (map (comp str/upper-case name)
                                            allowed-methods)))))


(defn nyi
  [code-name]
  (error-response 500 :not-implemented (str code-name " is not yet implemented")))


; TODO: deprecate this
(defn render
  "Renders a Hiccup template data structure to HTML and returns a response with
  the correct content type."
  [template]
  (-> (hiccup/html template)
      (r/response)
      (r/content-type "text/html")
      (r/charset "utf-8")))
