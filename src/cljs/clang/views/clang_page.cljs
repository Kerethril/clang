(ns clang.views.clang-page
  (:require
    [clojure.string :refer [escape]]
    [reagent.core :as reagent :refer [atom]]
    [clang.components.preloader :refer [asset-preloader loading-screen make-preloader-atom]]
    [clang.components.music-player :refer [make-playlist music-player]]
    [clang.components.clickboard :refer [clickboard]]))

(def default-settings
  {:background-url "/img/leangrygattsuman.jpg"
   :background-interlace true})

(defn style-overrides [overrides]
  [:style
   (str
     "#clang-page .clickboard {background-image:"
     "linear-gradient(to bottom, transparent, transparent 50%, black),"
     (when (:background-interlace overrides)
       (str "repeating-linear-gradient(to right, transparent, transparent 1px, black 1px, black 2px),"
            "repeating-linear-gradient(to top, transparent, transparent 1px, black 1px, black 2px),"))
     "url('" (escape (:background-url overrides) {\" "" \' ""}) "');}")])

(defn clang-page
  ([] (clang-page {}))
  ([setting-overrides]
   (let [settings (merge default-settings setting-overrides)
         assets {:background-img {:url (:background-url settings)
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
                  :rel  "stylesheet"}]
          (style-overrides settings)]
         (if (:ready? @preloader-atom)
           [(when @music-on? [music-player playlist])
            [clickboard (get-in assets [:clang-audio :url]) #(reset! music-on? true)]
            [:footer [:strong "Greatness"]]]
           [[loading-screen preloader-atom]]))))))