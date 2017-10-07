(ns exquisite-corpse.rest
  (:require
   [goog.net.XhrIo :as xhr]
   [cljs.core.async :as async :refer [>! <! put! chan close!]]
   
   [exquisite-corpse.util :refer [log elog json-serialize]])
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop alt!]]))

(def API "http://localhost:3000")

(defn GET [url]
  (log (str "GET " url))
  
  (let [ch (chan 1) url (str API url)]
    (xhr/send url
              (fn [e]
                (let [res (js->clj (-> e .-target .getResponseJson) :keywordize-keys true)]
                  (go (>! ch res)
                      (close! ch)))))
    ch))

(defn POST [url body]
  (log (str "POST " url " body") body)
  
  (let [ch (chan 1) url (str API url)]
    (xhr/send url
              (fn [e]
                (let [res (js->clj (-> e .-target .getResponseJson) :keywordize-keys true)]
                  (go (>! ch res)
                      (close! ch))))
              "POST"
              (json-serialize body)
              {"Content-Type" "application/json"})
    ch))

(defn PATCH [url body]
  (log (str "PATCH " url " body") body)
  
  (let [ch (chan 1) url (str API url)]
    (xhr/send url
              (fn [e]
                (let [res (js->clj (-> e .-target .getResponseJson) :keywordize-keys true)]
                  (go (>! ch res)
                      (close! ch))))
              "PATCH"
              (json-serialize body)
              {"Content-Type" "application/json"})
    ch))
