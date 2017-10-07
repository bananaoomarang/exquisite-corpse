(ns exquisite-corpse.state
  (:require [reagent.core :as reagent :refer [atom]]))

(defonce app-state (atom {
                          :title   "Exquisite Corpse"
                          :current-room nil}))

(defonce story (atom {:story []
                      :id ""}))
