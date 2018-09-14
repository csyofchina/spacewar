(ns spacewar.ui.widgets
  (:require [quil.core :as q]
            [spacewar.ui.protocols :as p]))

(deftype button [state]
  p/Drawable
  (draw [_]
    (let [{:keys [x y w h name color mouse-in left-down]} state]
      (q/stroke-weight 2)
      (if mouse-in
        (q/stroke 255 255 255)
        (apply q/stroke color))
      (if left-down
        (q/fill 255 255 255)
        (apply q/fill color))
      (q/rect x y w h h)
      (q/text-align :right :bottom)
      (q/text-font (:lcars (q/state :fonts)) 18)
      (q/fill 0 0 0)
      (q/text name (+ x w -10) (+ y h))))

  (setup [this] this)
  (update-state [_ _]
    (let [{:keys [x y w h]} state
          last-left-down (:left-down state)
          mx (q/mouse-x)
          my (q/mouse-y)
          mouse-in (and (>= mx x) (< mx (+ x w)) (>= my y) (< my (+ y h)))
          left-down (and mouse-in (q/mouse-pressed?) (= :left (q/mouse-button)))
          new-state (assoc state :mouse-in mouse-in :left-down left-down)
          event (if (and (not left-down) last-left-down mouse-in)
                  (:left-up-event state)
                  nil)]
      (p/pack-update (button. new-state) event))))

(deftype indicator-light [state]
  p/Drawable
  (draw [_]
    (let [{:keys [x y w h level draw-func colors]} state]
      (q/stroke 0 0 0)
      (q/stroke-weight 1)
      (apply q/fill (nth colors level))
      (draw-func x y w h)))

  (setup [_] (indicator-light. (assoc state :level 0)))
  (update-state [this _] (p/pack-update this))
  (get-state [_] state))

(deftype named-indicator [state]
  p/Drawable
  (draw [_]
    (let [{:keys [x y name]} state]
      (p/draw-elements state)
      (q/fill 0 0 0)
      (q/text-align :left :top)
      (q/text-font (:lcars (q/state :fonts)) 18)
      (q/text name (+ x 25) y)))

  (setup [_] (named-indicator.
               (assoc state :indicator (p/setup
                                         (->indicator-light state))
                            :elements [:indicator])))

  (update-state [_ commands]
    (let [[new-state events] (p/update-elements state commands)]
      (p/pack-update
        (named-indicator. new-state)
        events))))

