(ns games.frame
  (:import (javax.swing JFrame)))

(defn frame
  [content & {:keys [title size]
              :or {title "Untitled", size [300 300]}}]
  (doto (JFrame.)
    (.add content)
    (.setTitle title)
    (.setSize (first size) (second size))
    (.setLocationRelativeTo nil)
    (.setVisible true)))

(defn load-resource [res]
  (.getResource (clojure.lang.RT/baseLoader) res))
