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
