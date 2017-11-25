(ns exquisite-corpse.components
  (:require
   [reagent.core :refer [atom]]

   [exquisite-corpse.state :refer [app-state story]]
   [exquisite-corpse.sockets :as sockets]
   [exquisite-corpse.rest :refer [GET POST PATCH]]
   [exquisite-corpse.effects :refer [create-story add-line get-story]]
   [exquisite-corpse.state-utils :refer [get-story-lines is-finished?]]
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
                                              (add-line val)
                                              (sockets/send-message! app-state
                                                                     {:type :add-line
                                                                      :body {:line val}}))))
(defn email-please []
  [:form
   [:input.textarea {:placeholder "email"
                     :type "email"}]
   [:input.btn {:type "submit"
                :value "Update me"}]])

(defn get-story-lines []
  "Returns a list of lines to show"
  (let [lines    (:lines @story)
        viewing? (= 10 (count lines))]

    (if viewing?
      lines
      (take-last 1 lines))))

(defn line [key line]
  "Display a line"
  (log (:text line))
  ^{:key key}

  [:p.story-line (:text line)])

(defn app-root []
  (let [line-count    (count (:story @story))
        id            (:id @story)
        display-lines (get-story-lines)]
    [:div
     [:div.text-center.title
      [:h1 (:title @app-state)]]

     [:div.btn-group.text-center
      (button "Load random" (fn [_]
                                    (get-story)))
      (button "Create new" create-story)]

     [:div.story-wrapper
      (map-indexed line display-lines)]

     [:div.text-center
      (if (and
           (< line-count 10)
           (not= (:user-id @app-state) (:author (last display-lines))))
        [get-typin-box])]]))
