(ns merkledag.server.views
  (:require
    [bidi.bidi :as bidi]
    [merkledag.codecs.edn :as edn]
    [merkledag.server.routes :as route]
    [multihash.core :as multihash]
    [puget.color :as color]
    [puget.dispatch :as dispatch]
    [puget.printer :as puget]))


;; ## Page Layout Components

(defn- head
  "Generates an html <head> section."
  [title & extra]
  [:head
   [:title (str "MD" (when title (str \space title)))]
   [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
   [:link {:rel "stylesheet" :href "/styles/site.css"}]
   [:link {:rel "shortcut icon" :href "/favicon.ico"}]
   extra])


(defn- layout
  "Generates an html page based on the standard template."
  [head-content & body-content]
  [:html
   head-content
   [:body {:role "document"}
    ;(navbar)
    [:div.container {:role "main"} body-content]
    [:script {:src "/scripts/site.js"}]]])



;; ## Partials

(defn pretty-render
  [types value]
  [:pre
   (puget/pprint-str
     value
     {:print-color true
      :color-markup :html-inline
      :color-scheme {:keyword [:green]}
      :print-handlers (dispatch/chained-lookup
                        {merkledag.link.MerkleLink
                         (fn [printer link]
                           [:group
                            (color/document printer :tag "#data/link")
                            " "
                            [:span
                             (format "<a href=\"%s\" title=\"%s bytes\">"
                                     (route/path-for-node (:target link))
                                     (:tsize link "?"))
                             (:name link)
                             "</a>"]])}
                        (edn/types->print-handlers types))})])



;; ## Page Views

(defn index
  "Generate a hiccup document for the index page."
  []
  (layout
    (head "Index")
    [:h1 "Hello, Merkledag"]))


(defn show-node
  [types node]
  (layout
    (head (str "Node " (:id node)))
    [:h1 (multihash/base58 (:id node))]
    [:p (:id node)]
    [:p [:strong "Size: "] (:size node) " bytes"]
    [:p [:strong "Encoding: "] (interpose ", " (map #(vector :code % ) (:encoding node)))]
    (when (:links node)
      [:div
       [:h2 "Links"]
       [:ol (map (fn [link]
                   [:li
                    [:a {:href (route/path-for-block (:target link))}
                     (multihash/base58 (:target link))]
                    " "
                    [:a {:href (route/path-for-node (:target link))}
                     (:name link)]
                    (when (:tsize link)
                      (format " (%d bytes)" (:tsize link)))])
                 (:links node))]])
    (when (:data node)
      [:div
       [:h2 "Data"]
       (pretty-render types (:data node))])))
