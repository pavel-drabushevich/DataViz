(ns dataviz.connector
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [datascript.core :as ds]
  			[cljs-http.client :as http]
  			[cljs.core.async :refer [<!]]
  )
)

(defrecord IssueData [id title state])

(defn import 
  [db-created-cont]
  (load-from-outside "https://api.github.com/repos/TargetProcess/tauCharts/issues" db-created-cont)
)

(defn load-from-outside
  [url db-created-cont]
  (prn "going to fetch from" url)
  (go (let [response (<! (http/get url {:with-credentials? false}))]
  	      (prn "fetched from" url)
	      (prn "status code" (:status response))
	      (def data (map (fn[x] (IssueData. (:id x) (:title x) (:state x))) (:body response)))
	      (prn "fetched issues" data)
	      (store data db-created-cont)
       )
  )
)

(defn store
  [data db-created-cont]
  (def db-data (map (fn[item] {:id (:id item), :title (:title item), :state (:state item)}) data))
  (comment (prn "data to store" db-data))
  (def schema
      { 
      	  :id  {:db/axis :db.axis/none :db/card :db.card/available} 
      	  :title  {:db/axis :db.axis/none :db/card :db.card/available} 
      	  :state  {:db/axis :db.axis/available :db/card :db.card/available} 
      }
  )

  (def db (-> (ds/empty-db schema)
            (ds/db-with db-data)
          )
  )
  (comment (prn "data script db data" db))
  (db-created-cont db)
)
