(ns user
  (:require
    [clojure.java.io :as io]
    [clojure.repl :refer :all]
    [clojure.stacktrace :refer [print-cause-trace]]
    [clojure.string :as str]
    [clojure.tools.namespace.repl :refer [refresh]]
    (com.stuartsierra
      [component :as component]
      [dependency :as dependency])
    [environ.core :refer [env]]
    [merkledag.server.main :refer [system init! start! stop!]]))


(defn go!
  "Initializes with the default config and starts the system."
  []
  ; TODO: Configure the server somehow...
  (init!)
  (start!))


(defn reload!
  "Reloads all changed namespaces to update code, then re-launches the system."
  []
  (stop!)
  (refresh :after 'user/go!))
