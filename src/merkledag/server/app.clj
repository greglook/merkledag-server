(ns merkledag.server.app
  "This namespace defines a Ring handler binding URL paths to data lookup and
  controller actions."
  (:require
    [clojure.tools.logging :as log]
    (compojure
      [core :as compojure :refer [ANY GET POST]]
      [route :as route])
    (merkledag.server
      [response :refer :all]
      [urls :as urls]
      [views :as views])))


(defn wrap-url-context
  "Binds the given context for url rendering in views."
  [handler urls]
  (fn [request]
    (urls/with-context urls
      (handler request))))


(defn ring-handler
  "Constructs a new Ring handler implementing the application."
  [controller]
  (compojure/routes
    ; General index view.
    (GET "/" []
      (render (views/index)))
    (ANY "/" []
      (method-not-allowed :get))

    (route/not-found "Not Found")))
