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
  (let [nx (mod (+ (:x craft) (:dx craft)) 400)
        ny (mod (+ (:y craft) (:dy craft)) 300)]
    (assoc craft :x nx :y ny)))

(defn- direction [key-code]
  (cond
   (= key-code KeyEvent/VK_LEFT) [:dx -1]
   (= key-code KeyEvent/VK_RIGHT) [:dx 1]
   (= key-code KeyEvent/VK_UP) [:dy -1]
   (= key-code KeyEvent/VK_DOWN) [:dy 1]))

(defn- move [craft key-code d]
  (if-let [[dxy modifier] (direction key-code)]
    (assoc craft dxy (* modifier d))
    craft))

(defn- accelerate [craft key-code d]
  (if-let [[dxy modifier] (direction key-code)]
    (assoc craft dxy (+ (get craft dxy) (* d modifier)))
    craft))

(defn- make-board
  ([] (make-board accelerate))
  ([fn] (let [craft (ref (make-craft 40 60))

              t-adapter (proxy [KeyAdapter]
                            []

                          (keyPressed [e]
                            (dosync (alter craft fn (.getKeyCode e) 1)))

                          (keyReleased [e]
                            (dosync (alter craft fn (.getKeyCode e) 0))))

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
          (.setBackground board Color/GREEN)
          (.setDoubleBuffered board true)
          (.start timer)
          board)))

(defn r-type [fn]
  (frame (make-board fn) :title "R - Type" :size [400 300]))
