(ns exquisite-corpse.api
  (:require [exquisite-corpse.rest :refer [GET POST PATCH]]))

(defn create-story
  "Create a new story on the backend"
  [story]
  (POST "/story" story))

(defn get-story
  "Get a story from the backend, either random or by its id"
  ([]
   (GET "/story"))

  ([id]
   (GET (str "/story/" id))))

(defn update-story
  "Update story on the backend found by its id"
  [id next-line]
  (PATCH (str "/story/" id) { :line next-line }))

(defn get-top-stories [finished?]
  (GET "/stories/top" { :finished? finished? }))

(defn get-active-stories []
  (GET "/stories/active"))
