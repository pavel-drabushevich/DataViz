(ns dataviz.core
  (:require
    [clojure.string :as str]
    [cemerick.url :as u]
    [cljs.reader :refer [read-string]]
    [cljs-http.client :as http]
    [cljs.core.async :refer [<!]]
    [dataviz.ui :as ui]
    [dataviz.connector :as c]
    [cljs.core.async :as async]
    [datascript.core :as data]
    [datascript.transit :as dt]
    [quiescent.core :as q :include-macros true]
    [quiescent.dom :as dom])
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  )

(enable-console-print!)

(defn open-board
  [type rep]
    (defn make-slice [db, x, y]
        (prn "x" x)
        (prn "y" y)
        (def s (:schema db))
        (prn "schema" s)
        (defn axis [a]
          (if (= a "none") 
            `()
            (map first
              (data/q '[:find ?value
                  :in $ [[[?attr [[?aprop ?avalue] ...]] ...] ?t]
                  :where [(= ?attr ?t)]
                         [(= ?avalue :db.axis/available)]
                  [?entity ?attr ?value]]
              db [s (keyword a)]))))
        (def xaxis (axis x))
        (def yaxis (axis y))
        (def cells `())

        (prn "xaxis" xaxis)
        (prn "yaxis" yaxis)
        (prn "cells" cells)

        {:xaxis {:id x :values xaxis} :yaxis {:id y :values yaxis} :cells cells}
      )

    (defn prepare-attr [db]
      (map name (keys
        (filter
          (fn [[k v]]
              (= (:db/axis v) :db.axis/available))
          (:schema db)))))

    (c/import rep (fn[db]
        (prn "db data = " db)
        (prn "db metadata = " (:schema db))


        (def schema (prepare-attr db))
        (def mk (partial make-slice db))
        (defn update [x y]
            (def data (mk x y))
            (ui/render-board schema data update)
          )
        (update (first schema) (last schema))

        (prn ((partial make-slice db) :state :title))
        (prn (data/q '[:find ?entity ?attr ?value
                :in $ [[[?attr [[?aprop ?avalue] ...]] ...] ?t]
                :where [(= ?avalue :db.card/available)]
                [?entity ?attr ?value]]
              db [s a]))
      ) rep)
    )

(defn ^:export start
  []
    (ui/render-home (fn [type rep]
        (open-board type rep)
      ))
  )
