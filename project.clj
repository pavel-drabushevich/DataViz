(defproject dataviz "0.1.0"
  :plugins [[lein-cljsbuild "1.1.1"]]
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.228"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [datascript "0.13.0"]
                 [quiescent/quiescent "0.2.0-RC1"]
                 [cljs-http "0.1.38"]
                 [cljsjs/fixed-data-table "0.4.1-0" :exclusions [cljsjs/react]]]
  :resource-paths ["resources" "lib"]
  :hooks [leiningen.cljsbuild]
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
          :output-to     "dataviz.js"
          :optimizations :advanced
          :preamble      ["react-0.13.3.min.js"]
          :pretty-print  false
        }}
  ]})

