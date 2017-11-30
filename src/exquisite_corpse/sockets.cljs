(ns exquisite-corpse.sockets
  (:require
   [chord.client :refer [ws-ch]]
   [cljs.core.async :as async :refer [>! <! put! chan close!]]

   [exquisite-corpse.effects :refer [update-local-story!]]
   [exquisite-corpse.state :refer [app-state]]
   [exquisite-corpse.state-utils :refer [add-line]]
   [exquisite-corpse.util :refer [log elog json-serialize]])
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop alt!]]
   [exquisite-corpse.macros :refer [is-dev?]]))

(def socket-url (if (is-dev?)
                  "ws://localhost:3000/chord"
                  "wss://wordsports.xyz/api/chord"))

(defn handle-user-action [user-id action]
  (let [type (:type action)
        body (:body action)]
    (condp = type
      :add-line (update-local-story! (add-line (:story @app-state) (:line body) user-id))
      :ping     (log "pong")

      (log (str "Unexpected user action: " type)))))

(defn handle-joined [user-id user-count]
  (log "Hey, you're" user-id)
  (swap! app-state assoc :user-id user-id :user-count user-count))

(defn message-router [story {:keys [type message user-id user-count]}]
  (condp = type
    :user-joined (do
                   (log (str "User joined: "  user-id))
                   (swap! app-state assoc :user-count user-count))
    :user-left  (do
                  (log (str "User left :(: " user-id))
                  (swap! app-state assoc :user-count user-count))
    :it-you      (handle-joined user-id user-count)
    :user-action (handle-user-action user-id message)

    (log (str "Unrecognized message type :(: " type))))

(defn receive-messages [ws-channel]
  (go-loop []
    (let [data    (<! ws-channel)
          message (:message data)]
      (log "MY DATA" data)
      (if message
        (do
          (message-router (:story @app-state) message)
          (recur))
        (do
          (close! ws-channel)
          (log "DISCONNECTED"))))))

(defn init-websocket [id]
  (go
    (let [{:keys [ws-channel error]} (<! (ws-ch (str socket-url "/" id) {:format :transit-json}))]
      (if error
        (elog error)

        (do
          (receive-messages ws-channel)
          (swap! app-state assoc :current-room {:id id :ws-channel ws-channel}))))))

(defn handle-room-switch [new-id]
  (let [room (:current-room @app-state)]

    (if-not room
      (init-websocket new-id)

      (when-not (= new-id (:id room))
        (close! (:ws-channel room))
        (init-websocket new-id)))))
