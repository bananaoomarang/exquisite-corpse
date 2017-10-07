(ns exquisite-corpse.actions
  (:require
   [exquisite-corpse.state :refer [app-state story]]
   [exquisite-corpse.util :refer [log elog]]
   [exquisite-corpse.rest :refer [GET]]
   [exquisite-corpse.routing :as routing]
   [exquisite-corpse.sockets :as sockets])
  (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]]))

(defn load-story
  ([]
   (go
     (let [res       (<! (GET "/story"))
           id        (:id res)
           new-story (:story res)]
       (load-story id new-story))))
  
  ([id]
   (go
     (let [res       (<! (GET (str "/story/" id)))
           new-story (:story res)]
       (load-story id new-story))))

  ([id new-story]
   (swap! story assoc :id id :story new-story)
   (routing/nav! (str "/story/" id))
   (sockets/handle-room-switch id)))
