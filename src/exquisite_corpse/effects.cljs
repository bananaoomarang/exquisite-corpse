(ns exquisite-corpse.effects
  (:require
   [cljs.core.async :refer [<!]]

   [exquisite-corpse.util :refer [log elog]]
   [exquisite-corpse.state :refer [app-state default-story]]
   [exquisite-corpse.api :as api]
   [exquisite-corpse.state-utils :refer [is-finished?]])

  (:require-macros
   [cljs.core.async.macros :refer [go]]))

(defn create-story []
  (go
    (let [story (<! (api/create-story default-story))]

      (log story)
      (swap! app-state assoc :story story))))

(defn add-line [line]
  (let [story (:story @app-state)
        id    (:id    story)
        lines (:lines story)
        author (:user-id @app-state)]

    (when-not (is-finished? story)
      (go
        (let [new-story (<! (api/update-story id {:author author :text line}))]
          (swap! app-state assoc :story new-story))))))

(defn update-local-story [new-story]
  (swap! app-state assoc :story new-story))

(defn load-story
  ([]
   (go
     (let [story (<! (api/get-story))]

       (swap! app-state assoc :story story))))

  ([id]
   (go
     (let [story (<! (api/get-story id))]

       (swap! app-state assoc :story story)))))

(defn load-top-stories []
  (go
    (let [stories (<! (api/get-top-stories))]

      (swap! app-state assoc :top-stories stories))))
