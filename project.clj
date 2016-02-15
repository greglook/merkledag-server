(defproject mvxcvi/merkledag-server "0.1.0-SNAPSHOT"
  :description "HTTP interfaces for merkledag repositories."
  :url "https://github.com/greglook/merkledag-server"
  :license {:name "Public Domain"
            :url "http://unlicense.org/"}

  :deploy-branches ["master"]

  :dependencies
  [[bidi "1.21.1"]
   [com.cemerick/url "0.1.1" :exclusions [com.cemerick/clojurescript.test]]
   [ch.qos.logback/logback-classic "1.1.3"]
   [com.stuartsierra/component "0.3.0"]
   [environ "1.0.1"]
   [hiccup "1.0.5"]
   [mvxcvi/blocks "0.5.0"]
   [mvxcvi/merkledag "0.2.0-SNAPSHOT"]
   [mvxcvi/puget "1.0.0"]
   [org.clojure/clojure "1.8.0"]
   [org.clojure/tools.logging "0.3.1"]
   [ring/ring-core "1.4.0"]
   [ring/ring-jetty-adapter "1.4.0"]
   [ring-middleware-format "0.7.0"]]

  :hiera
  {:cluster-depth 2
   :ignore-ns #{clojure ring}
   :show-external true}

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
             :main merkledag.server.main}})
