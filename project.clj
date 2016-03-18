(defproject mvxcvi/merkledag-server "0.1.0-SNAPSHOT"
  :description "HTTP interfaces for merkledag repositories."
  :url "https://github.com/greglook/merkledag-server"
  :license {:name "Public Domain"
            :url "http://unlicense.org/"}

  :deploy-branches ["master"]

  :dependencies
  [[bidi "2.0.4"]
   [com.cemerick/url "0.1.1"
    :exclusions [com.cemerick/clojurescript.test]]
   [ch.qos.logback/logback-classic "1.1.6"]
   [com.stuartsierra/component "0.3.1"]
   [environ "1.0.2"]
   [mvxcvi/alphabase "0.2.0"]
   [mvxcvi/blocks "0.5.0"] ; TODO: upgrade to 0.6.1
   [mvxcvi/merkledag "0.2.0-SNAPSHOT"]
   [org.clojure/clojure "1.8.0"]
   [org.clojure/tools.logging "0.3.1"]
   [ring/ring-core "1.4.0"]
   [ring/ring-jetty-adapter "1.4.0"]
   [ring-cors "0.1.7"]
   [ring-middleware-format "0.7.0"]]

  :hiera
  {:cluster-depth 3
   :ignore-ns #{clojure ring}
   :show-external false}

  :whidbey
  {:tag-types {'blocks.data.Block {'blocks.data.Block (partial into {})}
               'merkledag.link.MerkleLink {'data/link (juxt :name :target :tsize)}
               'multihash.core.Multihash {'data/hash 'multihash.core/base58}}}

  :profiles
  {:repl {:source-paths ["dev/src"]
          :dependencies [[org.clojure/tools.namespace "0.2.10"]]
          :jvm-opts ["-DAPP_LOG_APPENDER=repl"
                     "-DAPP_LOG_LEVEL=DEBUG"]}

   :test {:jvm-opts ["-DAPP_LOG_APPENDER=nop"
                     "-DAPP_LOG_LEVEL=TRACE"]}

   :uberjar {:aot :all
             :target-path "target/uberjar"
             :main merkledag.server.core}})
