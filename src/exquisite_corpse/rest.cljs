(ns exquisite-corpse.rest
  (:require
   [goog.net.XhrIo :as xhr]
   [cljs.core.async :as async :refer [>! <! put! chan close!]]
   [exquisite-corpse.util :refer [log elog json-serialize]])

  (:require-macros
   [cljs.core.async.macros :refer [go go-loop alt!]]))

(def API "http://localhost:3000")

(defn- get-response-body [event]
  (let [headers      (js->clj (-> event .-target .getResponseHeaders))
        content-type (get headers "Content-Type")]
    (if (and
         (not (nil? content-type))
         (clojure.string/includes? content-type "application/json"))
      (js->clj (-> event .-target .getResponseJson) :keywordize-keys true)

      (-> event .-target .getResponse))))

(defn- get-req-handler [ch]
  (fn [event]
    (let [res (get-response-body event)]
      (put! ch res))))

(defn- make-xhr!
  ([ch method url]
   (log (str method " " url))
   (xhr/send url (get-req-handler ch) method))

  ([ch method url body]
   (log (str method " " url " with body " body))

   (xhr/send url (get-req-handler ch) method
             (json-serialize body)
             {"Content-Type" "application/json"})))

(defn- req [method url & rest]
  (let [ch  (chan 1)
        url (str API url)
        body (first rest)]

    (if body
      (make-xhr! ch method url body)
      (make-xhr! ch method url))

    ch))

(defn GET [url]
  (req "GET" url))

(defn POST [url body]
  (req "POST" url body))

(defn PATCH [url body]
  (req "PATCH" url body))
