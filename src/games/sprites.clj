(ns games.sprites
  (:use [games.utils :only [frame load-resource]])
  (:import (javax.swing ImageIcon JPanel Timer)
           (java.awt Color Toolkit)
           (java.awt.event ActionListener KeyAdapter KeyEvent)))

(defrecord Craft
    [x y dx dy image])

(defn- make-craft [x y]
  (let [ii (ImageIcon. (load-resource "craft.png"))
        img (.getImage ii)]
    (Craft. x y 0 0 img)))

(defn- move-craft [craft]
  (let [nx (+ (:x craft) (:dx craft))
        ny (+ (:y craft) (:dy craft))]
    (assoc craft :x nx :y ny)))

(defn- accelerate [craft key-code d]
  (letfn [(acceleration []
            (cond
             (= key-code KeyEvent/VK_LEFT) [:dx (- d)]
             (= key-code KeyEvent/VK_RIGHT) [:dx d]
             (= key-code KeyEvent/VK_UP) [:dy (- d)]
             (= key-code KeyEvent/VK_DOWN) [:dy d]))]
    (if-let [acc (acceleration)]
      (apply assoc craft acc)
      craft)))

(defn- make-board []
  (let [craft (ref (make-craft 40 60))

        t-adapter (proxy [KeyAdapter]
                      []

                    (keyPressed [e]
                      (dosync (alter craft accelerate (.getKeyCode e) 1)))

                    (keyReleased [e]
                      (dosync (alter craft accelerate (.getKeyCode e) 0))))

        board (proxy [JPanel ActionListener]
                  []

                (paint [g]
                  (proxy-super paint g)
                  (.drawImage g (:image @craft) (:x @craft) (:y @craft) this)
                  (-> (Toolkit/getDefaultToolkit) .sync)
                  (.dispose g))

                (actionPerformed [e]
                  (dosync (alter craft move-craft))
                  (proxy-super repaint)))

        timer (Timer. 5 board)]

    (.addKeyListener board t-adapter)
    (.setFocusable board true)
    (.setBackground board Color/WHITE)
    (.setDoubleBuffered board true)
    (.start timer)
    board))

(defn r-type []
  (frame (make-board) :title "R - Type" :size [400 300]))
