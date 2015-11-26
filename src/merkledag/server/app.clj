(ns merkledag.server.app
  "This namespace defines a Ring handler binding URL paths to data lookup and
  controller actions."
  (:require
    [clojure.tools.logging :as log]
    (merkledag.server
      [response :refer :all]
      [views :as views])))


(defn ring-handler
  "Constructs a new Ring handler implementing the application."
  [controller]
  #_
  (compojure/routes
    ; General index view.
    (GET "/" []
      (render (views/index)))
    (ANY "/" []
      (method-not-allowed :get))
    (route/not-found "Not Found")))
