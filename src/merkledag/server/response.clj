(ns merkledag.server.response
  "This namespace provides some simple helper functions for generating Ring
  response maps."
  (:require
    [clojure.string :as str]
    [ring.util.response :as r]))


;; ## Date Functions

(defn date-format
  "Constructs a new date formatter matching RFC 2616."
  ^java.text.SimpleDateFormat
  []
  (doto (java.text.SimpleDateFormat. "EEE, dd MMM YYYY HH:mm:ss Z")
    (.setTimeZone (java.util.TimeZone/getTimeZone "GMT"))))


(defn format-date
  "Formats a Java `Date` value into a date string."
  ^String
  [^java.util.Date date]
  (when date
    (.format (date-format) date)))


(defn parse-date
  "Parses a date string into a Java `Date` value."
  ^java.util.Date
  [^String string]
  (when string
    (.parse (date-format) string)))



;; ## Response Constructors

(defn error-response
  [status error-key message]
  (-> (r/response {:error error-key, :message message})
      (r/status status)))


(defn bad-request
  [message]
  (error-response 400 :bad-request message))


(defn not-found
  [message & {:as extra}]
  (-> (error-response 404 :not-found message)
      (update-in [:body] merge extra)))


(defn method-not-allowed
  [allowed-methods]
  (-> (error-response 405 :not-allowed "Method not allowed on this resource")
      (r/header "Allow" (str/join ", " (map (comp str/upper-case name)
                                            allowed-methods)))))

(defn nyi
  [code-name]
  (error-response 500 :not-implemented (str code-name " is not yet implemented")))



;; ## Error Monad

(defmacro wrap-try
  [let-spec err-form body]
  `(try
     (if-let ~let-spec
       ~body
       (let [~'ex nil]
         ~err-form))
     (catch Exception ~'ex
       ~err-form)))


(defmacro try-request
  [& forms]
  (when-not (odd? (count forms))
    (throw (IllegalArgumentException.
             (str "try-request must be called with an odd number of forms."
                  " Got: " (count forms)))))
  (if (= 1 (count forms))
    (first forms)
    (let [[let-spec err-form & more] forms]
      `(wrap-try
         ~let-spec ~err-form
         (try-request ~@more)))))
