(ns merkledag.server.routes
  "Route definition for the application.")


(def routes
  ["/" [["" :sys/index]
        ["blocks/" {"" :block/index
                    [:id] :block/resource}]
        ["nodes/" {"" :node/index
                   [:id] :node/resource
                   [:id "/links"] :node/links
                   [:id "/data"] :node/data}]]])
