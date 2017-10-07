(ns exquisite-corpse.components
  (:require
   [exquisite-corpse.state :refer [app-state story]]
   [exquisite-corpse.sockets :as sockets]
   [exquisite-corpse.rest :refer [GET POST PATCH]]
   [exquisite-corpse.actions :refer [load-story]]
   [exquisite-corpse.util :refer [log elog json-serialize]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]]))

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
                                              (update-story val)
                                              (sockets/send-message! app-state
                                                                     {:type :add-line
                                                                      :body {:line val}}))))

(defn get-story-lines []
  "Returns a list of lines to show"
  (let [lines    (:story @story)
        viewing? (= 10 (count lines))]

    (if viewing?
      lines
      (take-last 1 lines))))

(defn line [key line]
  "Display a line"
  ^{:key key}
  
  [:h3 line])

(defn app-root []
  [:div
   [:h1 "Exquisite Corpse"]
   [:div
    (map-indexed line (get-story-lines))]
   [:h3 (str "ID: " (:id @story))]
   [get-typin-box]
   [:div
    (button "Load random" (fn []
                            (load-story)))
    (button "Create new story" create-story)
    (button "Ping" #(sockets/send-message! app-state
                                           {:type :ping
                                            :body {:ping "pong"}}))]])
