(ns dataviz.ui
  (:require
    [quiescent.core :as q :include-macros true]
    [quiescent.dom :as d]
    [cljs.core.async :as async]))

(enable-console-print!)

(q/defcomponent Header 
  []
  (d/header {:id "header"}
            (d/h1 {} "DataViz")))

(q/defcomponent App
  []
  (d/div {}
         (Header)
         ))

(defn render 
  []
    (q/render (App) js/document.body)
  )
