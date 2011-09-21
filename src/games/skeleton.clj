(ns games.skeleton
  (:import (javax.swing JPanel JFrame)
           (java.awt BasicStroke Color Dimension Graphics Graphics2D RenderingHints)
           (java.awt.geom AffineTransform Ellipse2D$Double)))


(defn frame
  [content & {:keys [title size]
              :or {title "Untitled", size [300 300]}}]
  (doto (JFrame.)
    (.add content)
    (.setTitle title)
    (.setSize (first size) (second size))
    (.setLocationRelativeTo nil)
    (.setVisible true)))


(defn skeleton []
  (doto (frame (JPanel.) :title "Skeleton" :size [600 200])
    (.setResizable false)))


(defn donut-board []
  (proxy [JPanel]
      []
    (paint [g]
      (proxy-super paint g)
      (doto g
        (.setRenderingHints {RenderingHints/KEY_ANTIALIASING RenderingHints/VALUE_ANTIALIAS_ON
                             RenderingHints/KEY_RENDERING RenderingHints/VALUE_RENDER_QUALITY})
        (.setStroke (BasicStroke. 1))
        (.setColor Color/gray))

      (let [size (.getSize this)
            w (/ (.getWidth size) 2)
            h (/ (.getHeight size) 2)
            e (Ellipse2D$Double. 0 0 80 130)]
        (doseq [deg (range 0 360 5)]
          (let [at (AffineTransform/getTranslateInstance w h)]
            (.rotate at (Math/toRadians deg))
            (.draw g (.createTransformedShape at e))))))))

(defn donut []
  (frame (donut-board) :title "Donut" :size [360 310]))
