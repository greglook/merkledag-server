(ns merkledag.server.urls
  "Functions for generating URLs. These functions take a configuration map,
  which should contain the following entries:

  - `:server-url`   Base URL for the local server.
  - `:path-prefix`  Prefix to add to all paths on the local server."
  (:require
    [clojure.string :as str]))


(def ^:dynamic ^:no-doc *context*
  "Context to use for generating urls."
  nil)


(defmacro with-context
  "Executes the the given body of expressions with `*context*` bound to the
  given value."
  [context & body]
  `(binding [*context* (select-keys ~context [:server-url :path-prefix])]
     ~@body))


(defn- join
  "Joins together a number of segments with slashes (/)."
  [& path]
  (str/join \/ path))


(defn local-path
  "Constructs an absolute path to a resource on the server, taking into account
  the path prefix. `path` should not start with a slash."
  [& path]
  (str \/
       (when-let [prefix (:path-prefix *context*)]
         (str prefix \/))
       (str/join \/ path)))
