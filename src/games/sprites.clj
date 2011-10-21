(ns games.sprites
  (:use [games.utils :only [frame load-resource]])
  (:require [clojure.tools.logging :as logging])
  (:import (javax.swing ImageIcon JPanel Timer)
           (java.awt Color Toolkit)
           (java.awt.event ActionListener KeyAdapter KeyEvent)))

(def board-width 390)
(def missile-speed 2)

(let [craft (ImageIcon. (load-resource "craft.png"))
      missile (ImageIcon. (load-resource "missile.png"))]
  (def craft-image (.getImage craft))
  (def missile-image (.getImage missile)))

(def craft-width (.getWidth craft-image))
(def craft-height (.getHeight craft-image))

(defrecord Craft
    [x y dx dy image])

(defn- make-craft [x y]
  (Craft. x y 0 0 craft-image))

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

(defrecord Missile
    [x y visible? image])

(defn- make-missile [x y]
  (Missile. x y true missile-image))

(defn- move-missile [missile]
  (let [nx (+ (:x missile) missile-speed)]
    (assoc missile
      :x nx
      :visible? (< nx board-width))))

(defn- make-board
  ([] (make-board accelerate))
  ([fn] (let [craft (ref (make-craft 40 60))
              missiles (ref ())

              t-adapter (proxy [KeyAdapter]
                            []

                          (keyPressed [e]
                            (let [k-code (.getKeyCode e)]
                              ;; (logging/debug "keyPressed:" k-code)
                              (if (= k-code KeyEvent/VK_SPACE)
                                (dosync
                                 (alter missiles conj
                                        (make-missile (+ (:x @craft) craft-width)
                                                      (+ (:y @craft) (/ craft-height 2)))))
                                (dosync
                                 (alter craft fn (.getKeyCode e) 1)))))

                          (keyReleased [e]
                            (dosync (alter craft fn (.getKeyCode e) 0))))

              board (proxy [JPanel ActionListener]
                        []

                      (paint [g]
                        (proxy-super paint g)

                        (.drawImage g (:image @craft) (:x @craft) (:y @craft) this)

                        (doseq [m @missiles]
                          (.drawImage g (:image m) (:x m) (:y m) this))

                        (-> (Toolkit/getDefaultToolkit) .sync)
                        (.dispose g))

                      (actionPerformed [e]
                        (dosync

                         (alter craft move-craft)

                         (alter missiles #(filter :visible? (map move-missile %))))

                        (proxy-super repaint)))

              timer (Timer. 5 board)]

          (.addKeyListener board t-adapter)
          (.setFocusable board true)
          (.setBackground board Color/WHITE)
          (.setDoubleBuffered board true)
          (.start timer)
          board)))

(defn r-type [fn]
  (frame (make-board fn) :title "R - Type" :size [400 300]))
