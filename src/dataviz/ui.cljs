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
  (d/section {:id "panel-wizard"}
             (d/div {}
                    "Here you will be able to select "
                    (Selector {
                                :items ["A", "B", "C"]
                                :value "B"
                                :onChange #(println (str "X was changed to " %1))
                              })
                    " as X"
                    " and "
                    (Selector {
                                :items ["1", "2", "3"]
                                :value "2"
                                :onChange #(println (str "Y was changed to " %1))
                              })
                    " as Y"
             )
  ))

(q/defcomponent Option
  [item is-selected?]
  (d/option {:value item :selected is-selected?}
            item
  )
)

(q/defcomponent Selector
  [val]
  (def items (:items val))
  (def value (:value val))
  (def onChg (fn [e]
                 ((:onChange val) (.-value (.-currentTarget e)))
             ))
  (apply d/select
           {
             :className "selector"
             :onChange onChg
           }
           (map (fn [x] (Option x (= x value)))
                items)
  )
)

(q/defcomponent PanelResult
  []
  (d/section {:id "panel-result"}
             (d/div {} "Here will be a result")
  ))

(defn render
  []
  (q/render (App) js/document.body))
