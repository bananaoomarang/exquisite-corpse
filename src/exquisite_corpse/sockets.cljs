(ns exquisite-corpse.sockets
  (:require
   [chord.client :refer [ws-ch]]
   [cljs.core.async :as async :refer [>! <! put! chan close!]]

   [exquisite-corpse.effects :refer [update-local-story]]
   [exquisite-corpse.state :refer [app-state]]
   [exquisite-corpse.util :refer [log elog json-serialize]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]]))

(defn handle-user-action [story id action]
  (let [type (:type action)
        body (:body action)]
    (condp = type
      :add-line (update-local-story (assoc-in @app-state [:story :lines] (:line body)))
      :ping     (log "pong")

      (log (str "Unexpected user action: " type)))))

(defn handle-joined [user-id]
  (log "Hey, you're" user-id)
  (swap! app-state assoc :user-id user-id))

(defn message-router [story {:keys [type message user-id]}]
  (condp = type
    :user-joined (log (str "User joined: "  user-id))
    :user-left   (log (str "User left :(: " user-id))
    :it-you      (handle-joined             user-id)
    :user-action (handle-user-action story user-id message)

    (log (str "Unrecognized message type :(: " type))))

(defn receive-messages [ws-channel]
  (go-loop []
    (let [{:keys [message]} (<! ws-channel)]
      (if message
        (do
          (message-router (:story @app-state) message)
          (recur))
        (do
          (close! ws-channel)
          (log "DISCONNECTED"))))))

(defn init-websocket [id]
  (go
    (let [{:keys [ws-channel error]} (<! (ws-ch (str "ws://localhost:3000/chord/" id) {:format :transit-json}))]
      (if error
        (elog error)

        (do
          (receive-messages ws-channel)
          (swap! app-state assoc :current-room {:id id :ws-channel ws-channel}))))))

(defn send-message! [app-state msg]
  (let [room (:current-room @app-state)]
    (if-not room
      (log "Not in a room yet :(")

      (go
        (>! (:ws-channel room) msg)))))

(defn handle-room-switch [new-id]
  (let [room (:current-room @app-state)]

    (if-not room
      (init-websocket new-id)

      (when-not (= new-id (:id room))
        (close! (:ws-channel room))
        (init-websocket new-id)))))
