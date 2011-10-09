(ns games.animation
  (:use [games.frame :only [frame load-resource]])
  (:import (javax.swing JPanel ImageIcon Timer)
           (java.awt Color Toolkit)
           (java.awt.event ActionListener)))

(defn- swing-timer []
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
    (.setBackground board Color/BLACK)
    (.setDoubleBuffered board true)
    (.start timer)
    board))

(defn swing []
  (frame (swing-timer) :title "Swing" :size [280 240]))


(defn- util-timer []
  (let [ii (ImageIcon. (load-resource "yellow-rose.jpg"))
        img (.getImage ii)
        x (ref 10)
        y (ref 10)

        board (proxy [JPanel]
                  []
                (paint [g]
                  (proxy-super paint g)
                  (.drawImage g img @x @y this)
                  (-> (Toolkit/getDefaultToolkit) .sync)
                  (.dispose g)))

        schedule-task (proxy [java.util.TimerTask]
                          []
                        (run []
                          (dosync (alter x (fn [n] (if (> n 239) -45 (inc n))))
                                  (alter y (fn [n] (if (> n 239) -45 (inc n))))
                                  (.repaint board))))

        timer (java.util.Timer.)]
    (.setBackground board Color/BLACK)
    (.setDoubleBuffered board true)
    (.scheduleAtFixedRate timer schedule-task 100 10)
    board))

(defn util []
  (frame (util-timer) :title "Util" :size [280 240]))


(defn- thread-board []
  (let [ii (ImageIcon. (load-resource "yellow-rose.jpg"))
        img (.getImage ii)
        x (ref 10)
        y (ref 10)
        update-fn (ref nil)

        board (proxy [JPanel]
                  []
                (paint [g]
                  (proxy-super paint g)
                  (.drawImage g img @x @y this)
                  (-> (Toolkit/getDefaultToolkit) .sync)
                  (.dispose g))

                (addNotify []
                  (proxy-super addNotify)
                  (.start (Thread. @update-fn))))

        cycle-vals (fn []
                     (dosync (alter x (fn [n] (if (> n 239) -45 (inc n))))
                             (alter y (fn [n] (if (> n 239) -45 (inc n))))))

        update (fn []
                 (loop [at (System/currentTimeMillis)]
                   (cycle-vals)
                   (.repaint board)
                   (let [time-diff (- (System/currentTimeMillis) at)
                         sleep-time (if (< time-diff 50) (- 50 time-diff) 2)]
                     (Thread/sleep sleep-time))
                   (recur (System/currentTimeMillis))))
        ]
    (dosync (ref-set update-fn update))
    (.setBackground board Color/BLACK)
    (.setDoubleBuffered board true)
    board))

(defn thread-animation []
  (frame (thread-board) :title "Thread" :size [280 240]))
