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

(q/defcomponent Header
  []
  (d/header {:id "header"}
            (d/h1 {} "DataViz")))

(q/defcomponent Footer
  []
  (d/footer {:id "footer"}
            (d/p {} "(c) TauCrew 2015")))

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

(q/defcomponent PanelWizard
  [full-state]
  (d/section {:id "panel-wizard"}
             (d/div {}
                    "Here you will be able to select "
                    (Selector {
                                :items (:xs full-state)
                                :value (:x? full-state)
                                :onChange #((:update full-state) %1 (:y? full-state))
                              })
                    " as X"
                    " and "
                    (Selector {
                                :items (:ys full-state)
                                :value (:y? full-state)
                                :onChange #((:update full-state) (:x? full-state) %1)
                              })
                    " as Y"
             )
  ))

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
  [full-state]
  (def table (gen-table 100))
  (def doc-width (.-clientWidth js/document.body))
  (def col-width (/ doc-width (count (:xvalues full-state))))
  (prn "-----" (:xvalues full-state))
  (def columns (map #(Column #js {:label % :fixed true  :dataKey 0 :cellDataGetter getter :width col-width}) 
    (:xvalues full-state)))
  (d/section {:id "panel-result"}
             (Table
                #js {:width        doc-width
                     :height       600
                     :rowHeight    200
                     :rowGetter    #(get table %)
                     :rowsCount    (count table)
                     :headerHeight 50}
                columns
             )
  ))

(q/defcomponent Content
  [full-state]
  (d/section {:id "content"}
             (PanelWizard full-state)
             (PanelResult full-state)
  ))

(q/defcomponent App
  [full-state]
  (d/div {}
         (Header)
         (Content full-state)
         (Footer)
  ))

(defn app-state
  [schema data trigger-update]
    {
       :xs (conj schema "none")
       :ys (conj schema "none")
       :x? (:id (:xaxis data))
       :y? (:id (:yaxis data))
       :xvalues (conj (:values (:xaxis data)) "none")
       :yvalues (conj (:values (:yaxis data)) "none")
       :update trigger-update
    })

(defn render
  [schema data trigger-update]
;;(println slice-interceptor)
  (q/render (App (app-state schema data trigger-update)) js/document.body))
