(ns merkledag.server.views
  (:require
    (merkledag.server
      [helpers :refer :all]
      [urls :refer :all])))


;; ## Page Layout Components

(defn- head
  "Generates an html <head> section."
  [title & extra]
  [:head
   [:title (str "MD" (when title (str \space title)))]
   [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
   [:link {:rel "stylesheet" :href (local-path "css/site.css")}]
   [:link {:rel "shortcut icon" :href (local-path "favicon.ico")}]
   extra])


(defn- layout
  "Generates an html page based on the standard template."
  [head-content & body-content]
  [:html
   head-content
   [:body {:role "document"}
    ;(navbar)
    [:div.container {:role "main"} body-content]
    [:script {:src (local-path "js/site.js")}]]])



;; ## Partials

; ...



;; ## Page Views

(defn index
  "Generate a hiccup document for the index page."
  []
  (layout
    (head "Index")
    [:h1 "Hello, Merkledag"]))
