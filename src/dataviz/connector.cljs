(ns dataviz.connector
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [datascript.core :as data]
  			[cljs-http.client :as http]
  			[cljs.core.async :refer [<!]]
  )
)

(defrecord IssueData [title state])

(defn import 
  []
  (load-from-outside "https://api.github.com/repos/TargetProcess/tauCharts/issues")
)

(defn load-from-outside
  [url]
  (prn "going to fetch from" url)
  (go (let [response (<! (http/get url {:with-credentials? false}))]
  	      (prn "fetched from" url)
	      (prn "status code" (:status response))
	      (def data (map (fn[x] (IssueData. (:title x) (:state x))) (:body response)))
	      (prn "issues" data)
	      (store data)
       )
  )
)

(defn store
  [data]
)
