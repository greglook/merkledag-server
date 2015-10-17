(ns merkledag.server.helpers
  "Helper functions for generating markup for views."
  (:require
    [clojure.string :as str]))


;; ## Markup Helpers

(defn octicon
  "Renders an octicon span for the given shape."
  [shape]
  [:span {:class (str "octicon octicon-" shape)}])
