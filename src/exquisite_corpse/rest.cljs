(ns exquisite-corpse.rest
  (:require
   [goog.net.XhrIo :as xhr]
   [cljs.core.async :as async :refer [>! <! put! chan close!]]
   [exquisite-corpse.util :refer [log elog json-serialize]])
  (:import goog.Uri
           goog.Uri.QueryData)

  (:require-macros
   [cljs.core.async.macros :refer [go go-loop alt!]]
   [exquisite-corpse.macros :refer [is-dev?]]))

(def API (if (is-dev?) "http://localhost:3000" "https://wordsports.xyz/api"))

(defn- get-response-body [event]
  (let [headers      (js->clj (-> event .-target .getResponseHeaders))
        content-type (get headers "content-type")]

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
        body (first rest)
        params (second rest)
        uri (Uri. url)]

    (when params
      (.setQueryData uri (.createFromMap QueryData (clj->js params))))

    (if body
      (make-xhr! ch method uri body)
      (make-xhr! ch method uri))

    ch))

(defn GET
  ([url]
   (req "GET" url))
  ([url params]
   (req "GET" url false params)))

(defn POST [url body]
  (req "POST" url body))

(defn PATCH [url body]
  (req "PATCH" url body))
