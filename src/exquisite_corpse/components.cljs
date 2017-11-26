(ns exquisite-corpse.components
  (:require
   [reagent.core :refer [atom]]

   [exquisite-corpse.state :refer [app-state story]]
   [exquisite-corpse.rest :refer [GET POST PATCH]]
   [exquisite-corpse.effects :refer [create-story! add-line! load-story! nav! send-message!]]
   [exquisite-corpse.state-utils :refer [get-story-lines is-finished? get-story-first-line get-story-authors]]
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
                                              (add-line! val)
                                              (send-message! {:type :add-line
                                                              :body {:line val}}))))
(defn email-please []
  [:form
   [:input.textarea {:placeholder "email"
                     :type "email"}]
   [:input.btn {:type "submit"
                :value "Update me"}]])

(defn line [key line]
  "Display a line"
  ^{:key key}

  [:p.story-line (:text line)])

(defn story-card-opening [{:keys [lines]}]
  [:div.card-body
   (map-indexed line (take 3 lines))])

(defn story-card [story]
  [:div.card.clickable { :onClick #(nav! (str "/story/" (:id story)))}
   [:div.card-title (get-story-first-line story)]
   [story-card-opening story]])

(defn top-stories []
  "Displays grid of top stories"
  [:div.top-stories.cards
   (for [story (:top-stories @app-state)
         :let [id (:id story)]]

     ^{:key id}
     [story-card story])])

(defn app-root []
  (let [story         (:story @app-state)
        line-count    (count (:lines story))
        id            (:id story)
        display-lines (get-story-lines story)]

    [:div
     [:div.text-center.title
      [:h1 (:title @app-state)]]

     [:div.btn-group.text-center
      (button "Load random" (fn [_]
                                    (load-story!)))
      (button "Create new" create-story!)]

     [:div.story-wrapper
      (map-indexed line display-lines)]

     [:div.text-center
      (if (and
           (< line-count 10)
           (not= (:user-id @app-state) (:author (last display-lines))))
        [get-typin-box])]]))

(defn browse-root []
  [:div.browse-container
   [:div.browse-container-links
    [:h1.clickable {:onClick #(nav! (str "/browse/finished"))} "Finished Stories"]
    [:h1.clickable {:onClick #(nav! (str "/browse/unfinished"))} "Unfinished Stories"]]])

(defn browse-finished-root []
  [:div.browse-finished-container
   [:h1.text-center "Top Stories"]
   [top-stories]])

(defn browse-unfinished-root []
  [:div.browse-unfinished-container
   [:h1.text-center "These need some work…"]
   [top-stories]])

(defn about-root []
  [:div.about-container
   [:p "TODO"]])

(defn four-oh-four-root []
  [:div.404-container
   [:h1 "404"]
   [:p "I'm afraid you've lost me…"]])
