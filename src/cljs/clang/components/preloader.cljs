(ns clang.components.preloader
  (:require [reagent.core :as reagent :refer [atom]]))

(defn get-load-event-name
  [tag]
  (if (some #(= tag %) [:audio :video]) :onLoadedData :onLoad))

(defn make-preloader-atom [assets]
  (atom {:ready? false
         ;; Alter the provided map of assets to include a load-state.
         :assets (into {} (map (fn [[k v]] [k (assoc v :loaded false)]) assets))}))

(defn set-preloader-asset [preloader-atom asset-key asset-val]
  (let [p (assoc-in @preloader-atom [:assets asset-key :loaded] asset-val)
        assets (:assets p)
        asset-count (count assets)
        loaded-assets (reduce #(if (:loaded %2) (inc %1) %1) 0 (vals assets))
        load-progress (if (> asset-count 0) (/ loaded-assets asset-count) 1)
        ready (-> load-progress (>= 1) boolean)]
    (reset! preloader-atom (merge p {:ready? ready :load-progress load-progress}))))

(defn asset-preloader [preloader-atom]
  (into
    [:div {:style {:display "none"}}]
    (reduce
      (fn [r [k a]]
        (let [tag (:tag a)
              src (:url a)]
          (-> r
              (conj [:link {:rel "prefetch" :href src}])
              (conj [tag {:src src :preload "auto"
                          (get-load-event-name tag) #(set-preloader-asset preloader-atom k true)}]))))
      []
      (:assets @preloader-atom))))

(defn loading-screen [preloader]
  [:div.load-msg "Please wait... "
   [:h1 (* 100 (:load-progress @preloader)) "%"]])