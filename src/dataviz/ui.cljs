(ns dataviz.ui
  (:require
    [quiescent.core :as q :include-macros true]
    [quiescent.dom :as d]
    [cljs.core.async :as async]))

(enable-console-print!)

(q/defcomponent App
  []
  (d/div {}
         (Header)
         (Content)
         (Footer)
  ))

(q/defcomponent Header
  []
  (d/header {:id "header"}
            (d/h1 {} "DataViz")))

(q/defcomponent Content
  []
  (d/section {:id "content"}
             (PanelWizard)
             (PanelResult)
  ))

(q/defcomponent Footer
  []
  (d/footer {:id "footer"}
            (d/p {} "(c) TauCrew 2015")))

(q/defcomponent PanelWizard
  []
  (d/section {:id "content"}
             (d/div {} "Here you will be able to select X Y and items")
  ))

(q/defcomponent PanelResult
  []
  (d/section {:id "content"}
             (d/div {} "Here will be a result")
  ))

(defn render
  []
  (q/render (App) js/document.body))
