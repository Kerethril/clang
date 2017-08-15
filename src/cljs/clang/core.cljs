(ns clang.core
  (:require [reagent.core :as reagent :refer [atom]]
            [secretary.core :as secretary :include-macros true]
            [accountant.core :as accountant]))

;; -------------------------
;; Helpers and Components

(defn content-loaded? []
  (= (.-readyState js/document) "complete"))

(defn dom-loaded? []
  (contains? ["interactive" "complete"] (.-readyState js/document)))

(defn get-preloader-atom []
  (atom {:ready? false
         :assets {:background-img false
                  :clang-audio    false}}))

(defn set-preloader-asset [preloader asset-key asset-val]
  (let [p (assoc-in preloader [:assets asset-key] asset-val)
        assets (:assets p)
        asset-count (count assets)
        loaded-assets (reduce #(if %2 (inc %1) %1) 0 (vals assets))
        load-progress (if (> asset-count 0) (/ loaded-assets asset-count) 1)
        ready (-> load-progress (>= 1) boolean)]
    (merge p {:ready? ready :load-progress load-progress})))


(defn asset-preloader [preloader]
  [:div {:style {:display "none"}}
   [:link {:rel "prefetch" :href "/audio/clang.mp3"}]
   [:link {:rel "prefetch" :href "/img/leangrygattsuman.jpg"}]
   [:img
    {:src    "/img/leangrygattsuman.jpg"
     :onLoad #(swap! preloader set-preloader-asset :background-img true)}]
   [:audio
    {:src          "/audio/clang.mp3"
     :onLoadedData #(swap! preloader set-preloader-asset :clang-audio true)}]])

(defn clang [id x y subclass]
  [:div.clang {:key       id
               :draggable false
               :class     subclass
               :style     {:left x :top y}}])

(defn add-clang [clang-atom x y]
  (let [classes (for [x (range 1 10)] (str "type-" x))]
    (swap! clang-atom
           #(-> %
                (update :clangs conj (clang (:auto-increment %) x y (rand-nth classes)))
                (update :auto-increment inc)))))

(defn drop-clang [clang-atom]
  (swap! clang-atom #(update % :clangs subvec 1)))

(defn canvas []
  (let [clangs (atom {:clangs [] :auto-increment 0})
        clang-event (fn [e]
                      (add-clang clangs e.clientX e.clientY)
                      (js/setTimeout #(drop-clang clangs) 2000)
                      (.play (new js/Audio "/audio/clang.mp3")))]
    (fn []
      (into
        [:div#canvas {:onClick clang-event}]
        (:clangs @clangs)))))

(defn footer []
  [:footer [:strong "Greatness"]])

(defn loading-screen [preloader]
  [:div.load-msg "Please wait... "
   [:h1 (* 100 (:load-progress @preloader)) "%"]])

;; -------------------------
;; Views

(defn clang-page []
  (let [preloader (get-preloader-atom)]
    (fn []
      (into
        [:div#home-page
         [asset-preloader preloader]
         [:link {:type "text/css"
                 :href (str
                         "https://fonts.googleapis.com/css?family=Bangers|"
                         "Cabin+Sketch|Fredericka+the+Great|Frijole|Knewave|"
                         "Kranky|Londrina+Sketch|Ranchers|Bowlby+One+SC")
                 :rel  "stylesheet"}]]
        (if (:ready? @preloader)
          [[canvas] [footer]]
          [[loading-screen preloader]])))))


;; -------------------------
;; Routes

(def page (atom #'clang-page))

(defn current-page []
  [:div [@page]])

(secretary/defroute "/" [] (reset! page #'clang-page))

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
