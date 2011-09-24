(ns games.animation
  (:use [games.frame :only [frame load-resource]])
  (:import (javax.swing JPanel ImageIcon Timer)
           (java.awt Toolkit)
           (java.awt.event ActionListener)))

(defn swing-timer []
  (let [ii (ImageIcon. (load-resource "yellow-rose.jpg"))
        img (.getImage ii)
        x (ref 10)
        y (ref 10)

        board (proxy [JPanel ActionListener]
                  []

                (paint [g]
                  (proxy-super paint g)
                  (.drawImage g img @x @y this)
                  (-> (Toolkit/getDefaultToolkit) .sync)
                  (.dispose g))

                (actionPerformed [e]
                  (dosync (alter x (fn [n] (if (> n 239) -45 (inc n))))
                          (alter y (fn [n] (if (> n 239) -45 (inc n))))
                          (proxy-super repaint))))

        timer (Timer. 25 board)]
    (.setDoubleBuffered board true)
    (.start timer)
    board))

(defn swing []
  (frame (swing-timer) :title "Swing" :size [280 240]))
