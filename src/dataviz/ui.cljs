(ns dataviz.ui
  (:require
    [quiescent.core :as q :include-macros true]
    [quiescent.dom :as d]
    [quiescent.dom.uncontrolled :as du]
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

(q/defcomponent CellText
  [colVal, colIndex, fullRow]
  (d/div {:className "tick"}
         colVal))

(q/defcomponent Card
  [cardData]
  (d/div {:className "card"}
         (:title cardData)))

(q/defcomponent CellCards
  [colVal, colIndex, fullRow]
  (apply d/div
         {}
         (map (fn [colItem] (Card (:data colItem))) colVal))
)

(q/defcomponent PanelWizard
  [full-state]
  (d/section {:id "panel-wizard" :className "panel-wizard"}
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

(defn getter [k row]
  (nth row k))

(defn prepare-column [indx label]
  (Column #js {
                :label label
                :fixed true
                :dataKey indx
                :cellDataGetter getter
                :cellRenderer (if (= indx 0) CellText CellCards)
                :width col-width
              }))

(q/defcomponent PanelResult
  [full-state]
  (def doc-height (- (.-innerHeight js/window) 144))
  (def row-height (/ doc-height (count (:yvalues full-state))))
  (def doc-width  (.-clientWidth js/document.body))
  (def col-width  (/ (- doc-width 16) (+ (count (:xvalues full-state)) 1)))
  (def column-labels
    (conj (:xvalues full-state)
          (str (:y? full-state) "/" (:x? full-state))))

  (def columns (map-indexed prepare-column column-labels))
  (defn get-row [k]

    (def x-vals (:xvalues full-state))
    (def y-vals (:yvalues full-state))
    (def cells (:cells full-state))

    (def row-id (nth y-vals k))
    (def row-cells (doall (filter #(= (:y %) row-id) cells)))

    (def row (doall(map (fn [col-id]
                            (filter #(= (:x %) col-id) row-cells))
                        x-vals)))
    (conj row row-id)
  )
  (d/section {:id "panel-result"}
             (Table
                #js {:width        doc-width
                     :height       doc-height
                     :rowHeight    row-height
                     :rowGetter    get-row
                     :rowsCount    (count (:yvalues full-state))
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

(q/defcomponent Board
  [full-state]
  (d/div {}
         ;;(Header)
         (Content full-state)
         ;;(Footer)
  ))

(defn board-state
  [schema data trigger-update]
    {
       :xs (conj schema "none")
       :ys (conj schema "none")
       :x? (:id (:xaxis data))
       :y? (:id (:yaxis data))
       :xvalues (:values (:xaxis data))
       :yvalues (:values (:yaxis data))
       :cells (:cells data)
       :update trigger-update
    })

(def state-atom (atom {:source-type "github"
                       :input "TargetProcess/tauCharts"}))

(q/defcomponent Home
  [props]
  (d/div {:className "start-screen"}
         ;;(Header)
         (Selector {  :items `("github" "travis")
                      :value (:source-type @state-atom)
                      :onChange #(swap! state-atom assoc :source-type %)})
         (du/input {
              :placeholder "enter repo"
              :value (:input @state-atom)
              :style {:margin "10px"}
              :onChange (fn [evt]
                          (swap! state-atom assoc :input
                                 (.-value (.-target evt))))})
         (d/button {:onClick (fn [_]
                                ((:choose props) (:source-type @state-atom) (:input @state-atom)))}
            "Explore!")
         ;;(Footer)
  ))

(defn render-board
  [schema data trigger-update]
  (q/render (Board (board-state schema data trigger-update)) js/document.body))

(defn render-home
  [choose]
  (q/render (Home {:choose choose}) js/document.body))
