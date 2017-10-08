(ns exquisite-corpse.actions
  (:require
   [exquisite-corpse.state :refer [app-state story]]
   [exquisite-corpse.util :refer [log elog]]
   [exquisite-corpse.rest :refer [GET POST PATCH]])
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
   (swap! story assoc :id id :story new-story)))

(defn create-story []
  (go
    (let [res       (<! (POST "/story" {:story ["Once upon a time"]}))
          id        (:id res)
          new-story (:story res)]
      (swap! story assoc :id id :story new-story))))

(defn update-story [next-line]
  (go
    (let [res       (<! (PATCH (str "/story/" (:id @story)) {:nextLine next-line}))
          new-story (:story res)]
      (swap! story assoc :story new-story))))
