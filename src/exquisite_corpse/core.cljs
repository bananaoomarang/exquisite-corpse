(ns exquisite-corpse.core
  (:require [reagent.core :as reagent]

            [exquisite-corpse.state :as state]
            [exquisite-corpse.routing :as routing]
            [exquisite-corpse.sockets :as sockets]
            [exquisite-corpse.actions :refer [load-story]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]]))

(enable-console-print!)

(routing/init-app-routes)

(reagent/render [routing/current-page]
                (. js/document (getElementById "app")))
