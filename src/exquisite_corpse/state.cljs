(ns exquisite-corpse.state
  (:require [reagent.core :as reagent :refer [atom]]))

(def default-story {:max-line-count 10
                    :lines [{:author "Anon" :text "Once upon a time…"}]})

(defonce story (atom default-story))

(defonce app-state (atom {:title        "Exquisite Corpse"
                          :user-id      nil
                          :current-room nil
                          :story        default-story
                          :top-stories  []}))
