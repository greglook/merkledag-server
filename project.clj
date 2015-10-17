(defproject mvxcvi/merkledag-server "0.1.0-SNAPSHOT"
  :description "HTTP interfaces for merkledag repositories."
  :url "https://github.com/greglook/merkledag-server"
  :license {:name "Public Domain"
            :url "http://unlicense.org/"}

  :deploy-branches ["master"]

  :dependencies
  [[ch.qos.logback/logback-classic "1.1.3"]
   [compojure "1.4.0"]
   [com.stuartsierra/component "0.3.0"]
   [environ "1.0.1"]
   [org.clojure/clojure "1.7.0"]
   [org.clojure/tools.logging "0.3.1"]
   [ring/ring-core "1.4.0"]
   [ring/ring-jetty-adapter "1.4.0"]
   [ring-middleware-format "0.6.0"]]

  :profiles
  {:repl {:source-paths ["dev/src"]
          :dependencies [[org.clojure/tools.namespace "0.2.10"]]
          :jvm-opts ["-DAPP_LOG_APPENDER=repl"
                     "-DAPP_LOG_LEVEL=DEBUG"]}
   :test {:jvm-opts ["-DAPP_LOG_APPENDER=nop"
                     "-DAPP_LOG_LEVEL=TRACE"]}})
