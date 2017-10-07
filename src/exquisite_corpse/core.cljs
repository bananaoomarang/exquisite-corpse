(ns exquisite-corpse.core
  (:require [reagent.core :as reagent]

            [exquisite-corpse.state :as state]
            [exquisite-corpse.routing :as routing]
            [exquisite-corpse.sockets :as sockets]
            [exquisite-corpse.actions :refer [load-story]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]]))

(enable-console-print!)

(def current-page (routing/app-routes))

(reagent/render [routing/current-page]
                (. js/document (getElementById "app")))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
