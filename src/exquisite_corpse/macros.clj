(ns exquisite-corpse.macros)

(defmacro is-dev? []
  (not (= "production" (System/getenv "ENV"))))
