(ns clang.core
    (:require [reagent.core :as reagent :refer [atom]]
              [secretary.core :as secretary :include-macros true]
              [accountant.core :as accountant]))


;; -------------------------
;; Components

(def clang-lifespan 2000)

(defn clang! [id x y]
  [:div.clang {:key id
               :style
               {:left x
                :top y}}])

(defn add-clang [clang-atom x y]
  (swap! clang-atom #(-> %
                       (update :clangs conj (clang! (:auto-increment %) x y))
                       (update :auto-increment inc))))

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
            (js/setTimeout #(drop-clang clangs) clang-lifespan))}]
        (:clangs @clangs)))))


;; -------------------------
;; Views

(defn home-page []
  [:div [:h2 "Welcome to clang"]
   [:div [:a {:href "/about"} "go to about page"]]
   [canvas]])

(defn about-page []
  [:div [:h2 "About clang"]
   [:div [:a {:href "/"} "go to the home page"]]])

;; -------------------------
;; Routes

(def page (atom #'home-page))

(defn current-page []
  [:div [@page]])

(secretary/defroute "/" []
  (reset! page #'home-page))

(secretary/defroute "/about" []
  (reset! page #'about-page))

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
