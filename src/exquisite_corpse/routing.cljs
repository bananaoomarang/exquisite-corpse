(ns exquisite-corpse.routing
  (:require
   [secretary.core :as secretary]
   [goog.history.EventType :as EventType]
   [goog.events :as events]

   [exquisite-corpse.effects :refer [load-story! load-top-stories!]]
   [exquisite-corpse.state :refer [app-state history]]
   [exquisite-corpse.components :refer
    [app-root browse-root about-root four-oh-four-root browse-finished-root browse-unfinished-root]])
  (:require-macros
   [secretary.core :refer [defroute]]))

(defn hook-browser-navigation! []
  (doto history
    (events/listen
     EventType/NAVIGATE
     #(secretary/dispatch! (.-token %)))
    (.setEnabled true)))

(defmulti current-page #(@app-state :page))

(defmethod current-page :app []
  [app-root])

(defmethod current-page :browse []
  [browse-root])

(defmethod current-page :browse-finished []
  [browse-finished-root])

(defmethod current-page :browse-unfinished []
  [browse-unfinished-root])

(defmethod current-page :about []
  [about-root])

(defmethod current-page :404 []
  [four-oh-four-root])

(defn init-app-routes []
  (secretary/set-config! :prefix "#")

  (defroute "/" []
    (swap! app-state assoc :page :browse))

  (defroute "/story" []
    (swap! app-state assoc :page :app)
    (load-story!))

  (defroute "/story/:id" [id]
    (swap! app-state assoc :page :app)
    (when-not (= (-> @app-state :story :id) id)
      (load-story! id)))

  (defroute "/browse" []
    (swap! app-state assoc :page :browse))

  (defroute "/browse/finished" []
    (swap! app-state assoc :page :browse-finished)
    (load-top-stories! true))

  (defroute "/browse/unfinished" []
    (swap! app-state assoc :page :browse-unfinished)
    (load-top-stories! false))

  (defroute "/about" []
    (swap! app-state assoc :page :about))

  (defroute "*" []
    (swap! app-state assoc :page :404))

  (hook-browser-navigation!))
