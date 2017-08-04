(ns clang.core
    (:require [reagent.core :as reagent :refer [atom]]
              [secretary.core :as secretary :include-macros true]
              [accountant.core :as accountant]))


;; -------------------------
;; Components

(def clang-lifespan 2000)

(defn clang [id x y sub-class]
  [:div.clang {:key id
                :draggable false
                :class sub-class
                :style {:left x :top y}}])

(defn add-clang [clang-atom x y]
  (let [classes (for [x (range 1 10)] (str "type-" x))]
    (swap! clang-atom
      #(-> %
         (update :clangs conj (clang (:auto-increment %) x y (rand-nth classes)))
         (update :auto-increment inc)))))

(defn drop-clang [clang-atom]
  (swap! clang-atom #(update % :clangs subvec 1)))

(defn canvas []
  (let [clangs (atom {:clangs [] :auto-increment 0})]
    (fn []
      (into
        [:div#canvas
         {:onClick
          (fn [e]
            (add-clang clangs e.clientX e.clientY)
            (js/setTimeout #(drop-clang clangs) clang-lifespan)
            (.play (new js/Audio "/audio/clang.mp3")))}]
        (:clangs @clangs)))))

(defn footer []
  [:footer [:strong "Greatness"]])

;; -------------------------
;; Views

(defn home-page []
  [:div#home-page
   [:link {:type "text/css"
           :href "https://fonts.googleapis.com/css?family=Bangers|Bowlby+One+SC|Cabin+Sketch|Fredericka+the+Great|Frijole|Knewave|Kranky|Londrina+Sketch|Ranchers"
           :rel  "stylesheet"}]
   [canvas]
   [footer]])

;; -------------------------
;; Routes

(def page (atom #'home-page))

(defn current-page []
  [:div [@page]])

(secretary/defroute "/" []
  (reset! page #'home-page))

;; -------------------------
;; Initialize app

(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (accountant/configure-navigation!
    {:nav-handler
     (fn [path]
       (secretary/dispatch! path))
     :path-exists?
     (fn [path]
       (secretary/locate-route path))})
  (accountant/dispatch-current!)
  (mount-root))
