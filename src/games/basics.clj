(ns games.basics
  (:use [games.frame :only [frame load-resource]])
  (:import (javax.swing JPanel ImageIcon)
           (java.awt BasicStroke Color RenderingHints)
           (java.awt.geom AffineTransform Ellipse2D$Double)))


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

(defn image-board []
  (let [ii (ImageIcon. (load-resource "yellow-rose.jpg"))
        img (.getImage ii)]
   (proxy [JPanel]
       []
     (paint [g]
       (.drawImage g img 10 10 nil)))))


(defn image []
  (frame (image-board) :title "Yellow Rose" :size [280 240]))
