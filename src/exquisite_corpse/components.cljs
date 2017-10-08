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
        [:textarea.textarea
         {:placeholder placeholder
          :value @val
          :on-change (fn [e]
                       (reset! val (.. e -target -value)))}]
        [button "Add line" (fn [e]
                       (submit-handler @val)
                       (reset! val ""))]])))

(def get-typin-box (text-input "Then what?" (fn [val]
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
      (map #(str % "…") (take-last 1 lines)))))

(defn line [key line]
  "Display a line"
  ^{:key key}
  
  [:p.story-line line])

(defn app-root []
  (let [line-count (count (:story @story))
        id         (:id @story)]
    [:div
     [:div.text-center.title
      [:h1 "Exquisite Corpse"]]
     
     [:div.btn-group.text-center
      (button "Load random" (fn [_]
                              (load-story)))
      (button "Create new story" create-story)]

     [:div.story-wrapper
      (map-indexed line (get-story-lines))]
     
     [:div.text-center
      (if (< line-count 10) [get-typin-box])]]))

