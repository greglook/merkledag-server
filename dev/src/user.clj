(ns user
  (:require
    [clojure.java.io :as io]
    [clojure.repl :refer :all]
    [clojure.stacktrace :refer [print-cause-trace]]
    [clojure.string :as str]
    [clojure.tools.namespace.repl :refer [refresh]]
    [com.stuartsierra.component :as component]
    [environ.core :refer [env]]
    (merkledag.server
      [core :as core :refer [system start! stop!]]
      [main :refer [init!]])))


(defn go!
  "Initializes with the default config and starts the system."
  []
  (init!)
  (start!))


(defn reload!
  "Reloads all changed namespaces to update code, then re-launches the system."
  []
  (stop!)
  (refresh :after 'user/go!))