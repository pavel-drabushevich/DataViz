(ns dataviz.connector
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [datascript.core :as ds]
  			[cljs-http.client :as http]
  			[cljs.core.async :refer [<!]]
  )
)

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
	      (def data (map (fn[x] {:id (:id x), :title (:title x), :state (:state x), :body (:body x), :user (:login (:user x)) :assignee (if (nil? (:assignee x)) "none" (:login (:assignee x)))}) (:body response)))
	      (store data db-created-cont)
       )
  )
)

(defn store
  [data db-created-cont]
  (prn "data to db" data)
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
  (def db (-> (ds/empty-db schema) (ds/db-with data)))
  (db-created-cont db)
)
