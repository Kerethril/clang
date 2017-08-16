(ns clang.components.music-player
  (:require [reagent.core :as reagent :refer [atom]]))

(defn make-playlist
  "Creates a playlist atom with the provided track list.
   The track list is normally a vector of maps, containing :src and :title keys."
  [track-list]
  (atom {:current-index 0
         :current       (first track-list)
         :next          (second track-list)
         :previous      nil
         :track-list    track-list}))

(defn playlist-track-swap
  "Updates a playlist atom, switching the current track as well as it's neighbours.
  You need to supply the playlist atom as well as something to mutate the current
  track's index with. It can be a function which will be applied to the current index,
  or an explicit value."
  [playlist index-mutator]
  (let [cur (or (:current-index @playlist) 0)
        new (if (fn? index-mutator) (index-mutator cur) cur)
        ;; Make sure the new value is placed within the available bounds.
        min-val 0
        max-val (dec (count (:track-list @playlist)))
        normalised (-> new (min max-val) (max min-val))
        [current previous next] (for
                                  [x [normalised (dec normalised) (inc normalised)]]
                                  (nth (:track-list @playlist) x nil))]
    (swap! playlist merge {:current-index normalised
                           :current       current
                           :next          next
                           :previous      previous})))

(defn playlist-next
  "Jumps to next track if available."
  [playlist]
  (playlist-track-swap playlist inc))

(defn playlist-previous
  "Jumps to previous track if available."
  [playlist]
  (playlist-track-swap playlist dec))

(defn music-player
  "Music player component. Requires a playlist atom."
  [playlist]
  (let [current (:current @playlist)]
    [:div.music-player
     [:audio {:src (:src current) :controls true :autoPlay true :loop true}]
     [:div.playlist-controls
      [:button.previous {:onClick #(playlist-previous playlist)} "◀◀"]
      [:span.now-playing (or (:title current) "[ no audio track ]")]
      [:button.next {:onClick #(playlist-next playlist)} "►►"]]]))
