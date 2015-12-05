(ns dataviz.core
  (:require
    [dataviz.ui :as ui]
    [dataviz.connector :as c]
    [cljs.core.async :as async]
    [datascript.core :as data]))

(enable-console-print!)

(defn ^:export start 
  []
    (c/import)
    (ui/render)
  )
