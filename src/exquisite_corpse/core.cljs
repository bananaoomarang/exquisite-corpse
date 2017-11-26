(ns exquisite-corpse.core
  (:require [reagent.core :as reagent]

            [exquisite-corpse.state :refer [app-state]]
            [exquisite-corpse.effects :refer [nav!]]
            [exquisite-corpse.routing :as routing]
            [exquisite-corpse.sockets :as sockets])
  (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]]))

(enable-console-print!)

(routing/init-app-routes)

(reagent/render [routing/current-page]
                (. js/document (getElementById "app")))

(add-watch app-state :state-watcher
           (fn [_ _ prev-state new-state]
             (let [prev-id (-> prev-state :story :id)
                   new-id  (-> new-state :story :id)]

               (when-not (= prev-id new-id)
                 (nav! (str "/story/" new-id))
                 (sockets/handle-room-switch new-id)))))
