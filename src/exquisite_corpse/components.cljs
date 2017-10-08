(ns exquisite-corpse.components
  (:require
   [reagent.core :refer [atom]]
   
   [exquisite-corpse.state :refer [app-state story]]
   [exquisite-corpse.sockets :as sockets]
   [exquisite-corpse.rest :refer [GET POST PATCH]]
   [exquisite-corpse.actions :refer [load-story create-story update-story]]
   [exquisite-corpse.util :refer [log elog json-serialize]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]]))

(defn button [text handler]
  [:button.btn {:onClick handler} text])

(defn text-input [placeholder submit-handler]
   (let [val (atom "")]
     (fn []
       [:div
        [:textarea {:placeholder placeholder
                    :value @val
                    :on-change (fn [e]
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
