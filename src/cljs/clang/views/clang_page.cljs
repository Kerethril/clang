(ns clang.views.clang-page
  (:require
    [reagent.core :as reagent :refer [atom]]
    [clang.components.preloader :refer [asset-preloader loading-screen make-preloader-atom]]
    [clang.components.music-player :refer [make-playlist music-player]]
    [clang.components.clickboard :refer [clickboard]]))

(defn clang-page []
  (let [assets {:background-img {:url "/img/leangrygattsuman.jpg"
                                 :tag ':img}
                :clang-audio    {:url "/audio/clang.mp3"
                                 :tag ':audio}}
        preloader-atom (make-preloader-atom assets)
        music-on? (atom false)
        playlist (make-playlist [{:src "/audio/tell-me-why.mp3" :title "Tell me why"}
                                 {:src "/audio/inferno.mp3" :title "Inferno"}])]
    (fn []
      (into
        [:div#clang-page
         [asset-preloader preloader-atom]
         [:link {:type "text/css"
                 :href (str
                         "https://fonts.googleapis.com/css?family=Bangers|"
                         "Cabin+Sketch|Fredericka+the+Great|Frijole|Knewave|"
                         "Kranky|Londrina+Sketch|Ranchers|Bowlby+One+SC")
                 :rel  "stylesheet"}]]
        (if (:ready? @preloader-atom)
          [(when @music-on? [music-player playlist])
           [clickboard (get-in assets [:clang-audio :url]) #(reset! music-on? true)]
           [:footer [:strong "Greatness"]]]
          [[loading-screen preloader-atom]])))))