(ns exquisite-corpse.routing
  (:require
   [secretary.core :as secretary]
   [goog.history.EventType :as EventType]
   [goog.events :as events]

   [exquisite-corpse.effects :refer [load-story]]
   [exquisite-corpse.state :refer [app-state story]]
   [exquisite-corpse.components :refer [app-root]])
  (:import goog.History)
  (:require-macros
   [secretary.core :refer [defroute]]))

(defonce history (History.))

(defn nav! [token]
  (.setToken history token))

(defn hook-browser-navigation! []
  (doto history
    (events/listen
     EventType/NAVIGATE
     #(secretary/dispatch! (.-token %)))
    (.setEnabled true)))

(defmulti current-page #(@app-state :page))
(defmethod current-page :home []
  [app-root])

(defn init-app-routes []
  (secretary/set-config! :prefix "#")

  (defroute "/" []
    (swap! app-state assoc :page :home)
    (load-story))

  (defroute "/story" []
    (swap! app-state assoc :page :home)
    (load-story))

  (defroute "/story/:id" [id]
    (swap! app-state assoc :page :home)
    (load-story id))

  (hook-browser-navigation!))
