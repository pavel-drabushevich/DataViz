(ns dataviz.connector
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [datascript.core :as ds]
  			[cljs-http.client :as http]
  			[cljs.core.async :refer [<!]]
  )
)

(defrecord IssueData [id title state body user assignee])

(defn import 
  [url db-created-cont]
  (load-from-outside url db-created-cont)
)

(defn load-from-outside
  [rep db-created-cont]
  (def url (str "https://api.github.com/repos/" rep "/issues?state=all"))
  (prn "going to fetch from" url)
  (go (let [response (<! (http/get url {:with-credentials? false}))]
  	      (prn "fetched from" url)
	      (prn "status code" (:status response))
	      (def data (map (fn[x] (IssueData. (:id x) (:title x) (:state x) (:body x) (:login (:user x)) (if (nil? (:assignee x)) "none" (:login (:assignee x))))) (:body response)))
	      (store data db-created-cont)
       )
  )
)

(defn store
  [data db-created-cont]
  (prn "data to db" data)
  (def db-data (map (fn[item] {:id (:id item), :title (:title item), :state (:state item), :body (:body item), :user (:user item) :assignee (:assignee item) }) data))
  ;(prn "data to store" db-data)
  (def schema
      { 
      	  :id  {:db/axis :db.axis/none :db/card :db.card/available} 
      	  :title  {:db/axis :db.axis/none :db/card :db.card/available} 
      	  :state  {:db/axis :db.axis/available :db/card :db.card/available} 
      	  :body  {:db/axis :db.axis/none :db/card :db.card/available} 
      	  :user  {:db/axis :db.axis/available :db/card :db.card/available} 
      	  :assignee  {:db/axis :db.axis/available :db/card :db.card/available} 
      }
  )

  (def db (-> (ds/empty-db schema)
            (ds/db-with db-data)
          )
  )
  ;(prn "data script db data" db)
  (db-created-cont db)
)
