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
  [:button.btn {:on-click handler} text])

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
  [:div.card.clickable { :on-click #(nav! (str "/story/" (:id story)))}
   [:span.card-lil-info "Readers: " (:user-count story)]
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
        user-count    (:user-count @app-state)
        display-lines (get-story-lines story)]

    [:div
     [:div.text-center.title
      [:h1.clickable {:on-click #(nav! "/")} (:title @app-state)]]

     [:h3.text-center "Readers: " user-count]

     [:div.story-wrapper
      (map-indexed line display-lines)]

     [:div.text-center
      (if (and
           (< line-count 10)
           (not= (:user-id @app-state) (:author (last display-lines))))
        [get-typin-box])]]))

(defn toolbar []
  [:div.btn-group.text-center
   (button "Load random" (fn [_]
                           (load-story!)))
   (button "Create new" create-story!)])

(defn or-foot []
  [:div.or-foot-container
   [:h1.text-center "Or…"]
   [toolbar]])

(defn browse-root []
  [:div.browse-container
   [:div.browse-container-links
    [:h1.clickable {:on-click #(nav! (str "/browse/unfinished"))} "Unfinished Stories"]
    [:h1.clickable {:on-click #(nav! (str "/browse/finished"))} "Finished Stories"]]])

(defn browse-finished-root []
  [:div.browse-finished-container
   [:h1.text-center "Top Stories"]
   (if (= 0 (count (:top-stories @app-state)))
     [:h2.text-center "Nothing here :("]
     [top-stories])
   [or-foot]])

(defn browse-unfinished-root []
  [:div.browse-unfinished-container
   [:h1.text-center "These need some work…"]
   (if (= 0 (count (:top-stories @app-state)))
     [:h2.text-center "Nothing here :("]
     [top-stories])
   [or-foot]])

(defn about-root []
  [:div.about-container
   [:p "TODO"]])

(defn four-oh-four-root []
  [:div.404-container
   [:h1 "404"]
   [:p "I'm afraid you've lost me…"]])
