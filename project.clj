(defproject dataviz "0.1.0"
  :plugins [[lein-cljsbuild "1.1.1"]]
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.170"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [datascript "0.13.0"]
                 [datascript-transit "0.2.0"]
                 [quiescent/quiescent "0.2.0-RC1"]
                 [cljs-http "0.1.38"]
                 [cljsjs/fixed-data-table "0.4.1-0"]
                 [com.cemerick/url "0.1.1"]]
  :resource-paths ["resources" "lib"]
  :cljsbuild {
    :builds [
      { :id "none"
        :source-paths ["src"]
        :compiler {
          :main          dataviz.core
          :output-to     "target/dataviz.js"
          :output-dir    "target/none"
          :optimizations :none
          :source-map    true
        }}
      { :id "advanced"
        :source-paths ["src"]
        :compiler {
          :main          datascript-chat.core
          :output-to     "target/dataviz.js"
          :optimizations :advanced
          :pretty-print  false
        }}
  ]})

