(ns exquisite-corpse.core
  (:require [reagent.core :as reagent :refer [atom]]
            [goog.net.XhrIo :as xhr]
            [goog.json :as json]
            [cljs.core.async :as async :refer [>! <! put! chan close!]]
            [secretary.core :as secretary]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [chord.client :refer [ws-ch]])
  (:import goog.History)
  (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]]
                   [secretary.core :refer [defroute]]))

(enable-console-print!)

(def API "http://localhost:3000")

(defn log [& d]
  (apply (.-log js/console) (map clj->js d)))

(defn elog [& d]
  (apply (.-error js/console) (map clj->js d)))

(defn json-serialize [data]
  (.stringify js/JSON (clj->js data)))

(defonce history (History.))

(defn nav! [token]
  (.setToken history token))

(def new-msg-ch (chan))

(defn handle-user-action [id action]
  (let [type (:type action)
        body (:body action)]
    (condp = type
      :add-line (log "add line")
      :ping (log "pong")
      (log (str "Unexpected user action: " type)))))

(defn message-router [{:keys [type message user-id]}]
  (condp = type
    :user-joined (log (str "User joined: "  user-id))
    :user-left   (log (str "User left :(: " user-id))
    :user-action (handle-user-action user-id message)

    (log (str "Unrecognized message type :(: " type))))

(defn receive-messages [ws-channel]
  (go-loop []
    (let [{:keys [message]} (<! ws-channel)]
      (if message
        (do
          (message-router message)
          (recur))
        (do
          (close! ws-channel)
          (log "DISCONNECTED"))))))

(defn send-messages [ws-channel]
  (go-loop []
    (when-let [msg (<! new-msg-ch)]
      (>! ws-channel msg)
      (recur))))

(defn init-websocket []
  (go
    (let [{:keys [ws-channel error]} (<! (ws-ch "ws://localhost:3000/chord" {:format :transit-json}))]
      (if error
        (elog error)

        (do
          (receive-messages ws-channel)
          (send-messages    ws-channel))))))

(defn send-message! [msg]
  (go
    (>! new-msg-ch msg)))

;; TODO: unify

(defn GET [url]
  (let [ch (chan 1) url (str API url)]
    (xhr/send url
              (fn [e]
                (let [res (js->clj (-> e .-target .getResponseJson) :keywordize-keys true)]
                  (go (>! ch res)
                      (close! ch)))))
    ch))

(defn POST [url body]
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

(defn log-url [s]
  (go
    (log (<! (GET s)))))

(defn post-url [s body]
  (go
    (log (<! (POST s body)))))

(defn patch-url [s body]
  (go
    (log (<! (PATCH s body)))))

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {
                          :title   "Exquisite Corpse"
                          :viewing false }))

(defonce story (atom {:story []
                      :id ""}))

(defn button [text handler]
  [:button.btn {:onClick handler} text])

(defn create-story []
  (go
    (let [res       (<! (POST "/story" {:story ["Once upon a time..."]}))
          id        (:id res)
          new-story (:story res)]
      (swap! story assoc :id id :story new-story))))

(defn update-story [next-line]
  (go
    (let [res       (<! (PATCH (str "/story/" (:id @story)) {:nextLine next-line}))
          new-story (:story res)]
      (swap! story assoc :story new-story)
      (log (:story @story)))))

(defn load-story
  ([]
   (go
     (let [res       (<! (GET "/story"))
           id        (:id res)
           new-story (:story res)]
       (load-story id new-story))))
  
  ([id]
   (go
     (let [res       (<! (GET (str "/story/" id)))
           new-story (:story res)]
       (load-story id new-story))))

  ([id new-story]
   (log "STORY LOADED")
   (swap! story assoc :id id :story new-story)
   (nav! (str "/story/" id))))

(defn text-input [placeholder submit-handler]
   (let [val (atom "")]
     (fn []
       [:div
        [:textarea {:placeholder placeholder
                    :value @val
                    :on-change (fn [e]
                                 (log "resetting!")
                                 (reset! val (.. e -target -value)))}]
        [button "OK" (fn [e]
                       (submit-handler @val)
                       (reset! val ""))]])))

(def get-typin-box (text-input "Get typin’" (fn [val]
                                              (update-story val))))

(defn hello-world []
  [:div
   [:h1 "Exquisite Corpse"]
   [:div
    (for [line (:story @story)]
      ^{:key line}
      [:h3 line])]
   [:h3 (str "ID: " (:id @story))]
   [get-typin-box]
   [:div
    (button "Load random" (fn []
                            (load-story)))
    (button "Create new story" create-story)
    (button "Ping" #(send-message! {:type :ping
                                    :body {:ping "pong"}}))]])

;; ROUTING

(defn hook-browser-navigation! []
  (doto history
    (events/listen
     EventType/NAVIGATE
     (fn [e]
       (secretary/dispatch! (.-token e))))
    (.setEnabled true)))

(defn app-routes []
  (secretary/set-config! :prefix "#")

  (defroute "/" []
    (swap! app-state assoc :page :home)
    (load-story))
  
  (defroute "/about" []
    (swap! app-state assoc :page :about))

  (defroute "/story" []
    (swap! app-state assoc :page :home)
    (load-story))

  (defroute "/story/:id" [id]
    (swap! app-state assoc :page :home)
    (if-not (= id (:id @story))
      (load-story id)))

  (hook-browser-navigation!))

(defmulti current-page #(@app-state :page))
(defmethod current-page :home []
  [hello-world])
(defmethod current-page :about []
  [get-typin-box])

(init-websocket)
(app-routes)
(reagent/render [current-page]
                          (. js/document (getElementById "app")))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
