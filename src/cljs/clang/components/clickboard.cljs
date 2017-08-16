(ns clang.components.clickboard
  (:require [reagent.core :as reagent :refer [atom]]))

(defn spot [id x y subclass]
  [:div.spot {:key       id
              :draggable false
              :class     subclass
              :style     {:left x :top y}}])

(defn add-spot [spots-atom x y]
  (let [classes (for [x (range 1 10)] (str "type-" x))]
    (swap! spots-atom
           #(-> %
                (update
                  :spots conj (spot (:auto-increment %) x y (rand-nth classes)))
                (update :auto-increment inc)))))

(defn drop-spot [spots-atom]
  (swap! spots-atom #(update % :spots subvec 1)))

(defn get-spots-atom []
  (atom {:spots [] :auto-increment 0}))

(defn clickboard
  ([audio-url & on-click-hooks]
   (let [spots (get-spots-atom)
         spot-onclick (fn [e]
                        (.play (new js/Audio audio-url))
                        (add-spot spots e.clientX e.clientY)
                        (js/setTimeout #(drop-spot spots) 2000)
                        (doseq [f on-click-hooks] (f)))]
     (fn [] (into [:div.clickboard {:onClick spot-onclick}] (:spots @spots))))))