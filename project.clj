(defproject mvxcvi/merkledag-server "0.1.0-SNAPSHOT"
  :description "HTTP interfaces for merkledag repositories."
  :url "https://github.com/greglook/merkledag-server"
  :license {:name "Public Domain"
            :url "http://unlicense.org/"}

  :deploy-branches ["master"]

  :dependencies
  [[ch.qos.logback/logback-classic "1.1.2"]
   [compojure "1.3.1"]
   [com.stuartsierra/component "0.2.2"]
   [environ "1.0.0"]
   [org.clojure/clojure "1.7.0"]
   [org.clojure/tools.logging "0.3.1"]
   [ring/ring-core "1.3.2"]
   [ring/ring-jetty-adapter "1.3.2"]
   [ring-middleware-format "0.4.0"]]

  :profiles
  {:repl {:source-paths ["dev/src"]
          :dependencies [[org.clojure/tools.namespace "0.2.8"]]
          :jvm-opts ["-DAPP_LOG_APPENDER=repl"
                     "-DAPP_LOG_LEVEL=DEBUG"]}
   :test {:jvm-opts ["-DAPP_LOG_APPENDER=nop"
                     "-DAPP_LOG_LEVEL=TRACE"]}})
