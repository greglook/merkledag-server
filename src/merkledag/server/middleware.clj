(ns merkledag.server.middleware
  (:require
    [clojure.string :as str]
    [clojure.tools.logging :as log]
    [ring.util.response :as r]))


(defn- error-map
  "Converts an exception into a map for handler responses."
  [^Exception ex]
  (let [info {:error (.getName (class ex))
              :message (.getMessage ex)}
        cause (.getCause ex)]
    (if cause
      (assoc info :cause (.getName (class cause)))
      info)))


(defn wrap-exception-handler
  "Ring middleware to capture application exceptions and return a map of info
  about the error."
  [handler]
  (fn [request]
    (try
      (handler request)
      (catch Exception e
        (log/error e "Error while handling request!")
        (-> (r/response (error-map e))
            (r/status 500))))))


(defn wrap-request-logger
  "Ring middleware to log information about service requests."
  [handler logger-ns]
  (fn [{:keys [uri remote-addr request-method] :as request}]
    (let [start (System/nanoTime)
          method (str/upper-case (name request-method))]
      (log/log logger-ns :trace nil
               (format "%s %s %s" remote-addr method uri))
      (let [{:keys [status headers] :as response} (handler request)
            elapsed (/ (- (System/nanoTime) start) 1000000.0)
            msg (format "%s %s %s -> %s (%.3f ms)"
                        remote-addr method uri status elapsed)]
        (log/log logger-ns
                 (if (or (nil? status) (<= 400 status 599)) :warn :info)
                 nil msg)
        response))))


(defn wrap-x-forwarded-for
  "Ring middleware to fix the remote address if the request passed through
  proxies on the way to the service.

  This function replaces the request's :remote-addr with the LAST entry in the
  forwarded header. This gives the address of the host which called the service
  endpoint, which may not be the same as the original client."
  [handler]
  (fn [request]
    (if-let [xff (get-in request [:headers "x-forwarded-for"])]
      (let [addrs (str/split xff #"\s*,\s*")]
        (when (> (count addrs) 1)
          (log/trace "Multiple request forwards from"
                     (str/join " -> " addrs)
                     "->" (:remote-addr request)))
        (handler (assoc request :remote-addr (first addrs))))
      (handler request))))
