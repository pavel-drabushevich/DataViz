(ns dataviz.ui
  (:require
    [quiescent.core :as q :include-macros true]
    [quiescent.dom :as d]
    [cljsjs.fixed-data-table]
    [cljs.core.async :as async]))

;;; calling a React component directly is deprecated since v0.12
;;; http://fb.me/react-legacyfactory
(def Table (js/React.createFactory js/FixedDataTable.Table))
(def Column (js/React.createFactory js/FixedDataTable.Column))
(def ColumnGroup (js/React.createFactory js/FixedDataTable.ColumnGroup))

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



(defn gen-table
  "Generate `size` rows vector of 4 columns vectors to mock up the table."
  [size]
  (mapv (fn [i] [i                                                   ; Number
                 (rand-int 1000)                                     ; Amount
                 (rand)                                              ; Coeff
                 (rand-nth ["Here" "There" "Nowhere" "Somewhere"])]) ; Store
        (range 1 (inc size))))

(defn getter [k row] (get row k))

(q/defcomponent PanelResult
  []
  (def table (gen-table 100))
  (d/section {:id "panel-result"}
             (Table
                #js {:width        400
                     :height       600
                     :rowHeight    50
                     :rowGetter    #(get table %)
                     :rowsCount    (count table)
                     :headerHeight 50}
                (Column
                  #js {:label "Number" :dataKey 0 :cellDataGetter getter :width 100})
                (Column
                  #js {:label "Amount" :dataKey 1 :cellDataGetter getter :width 100})
                (Column
                  #js {:label "Coeff" :dataKey 2 :cellDataGetter getter :width 200})
                (Column
                  #js {:label "Store" :dataKey 3 :cellDataGetter getter :width 200})
             )
  ))

(defn render
  []
  (q/render (App) js/document.body))
