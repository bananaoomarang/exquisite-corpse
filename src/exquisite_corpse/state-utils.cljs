(ns exquisite-corpse.state-utils)

(defn is-finished? [story]
  (let [max-line-count (:max-line-count story)
        lines          (:lines story)
        line-count     (count lines)]
    (= line-count max-line-count)))

(defn get-story-lines [story]
  (let [lines (:lines story)]

    (if (is-finished? story)
      lines
      (take-last 1 lines))))

(defn get-story-first-line [story]
  (-> story
      :lines
      second
      :text))

(defn get-story-authors [{:keys [lines]}]
  (exquisite-corpse.util/log lines)
  (map :author lines))
