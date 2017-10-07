(ns exquisite-corpse.util)

(defn log [& d]
  (apply (.-log js/console) (map clj->js d)))

(defn elog [& d]
  (apply (.-error js/console) (map clj->js d)))

(defn json-serialize [data]
  (.stringify js/JSON (clj->js data)))
