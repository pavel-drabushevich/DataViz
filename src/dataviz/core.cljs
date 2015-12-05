(ns dataviz.core
  (:require
    [quiescent.core :as q :include-macros true]
    [quiescent.dom :as dom]
    [clojure.string :as str]
    [cljs.core.async :as async]
    [datascript.core :as data]))

(enable-console-print!)

(q/defcomponent Header 
  []
  (dom/header {:id "header"}
            (dom/h1 {} "DataViz")))

(q/defcomponent App
  []
  (dom/div {}
         (Header)
         ))

(defn ^:export start 
  []
    (q/render (App) js/document.body)
  )
