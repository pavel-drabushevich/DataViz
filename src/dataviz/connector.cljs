(ns dataviz.connector
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [datascript.core :as ds]
  			[cljs-http.client :as http]
  			[cljs.core.async :refer [<!]]
  			[dataviz.utils :as utils]
  )
)

(defn import 
  [repo source-type db-created-cont]
  (case source-type
  	:github (load-from-outside (build-github-url repo) convert-github-data-to-db-data build-github-db-schema db-created-cont)
  	:travis (load-from-outside (build-travis-url repo) convert-travis-data-to-db-data build-travis-db-schema db-created-cont)
  )
)

(defn load-from-outside
  [url convert-to-db-data build-db-schema db-created-cont]
  (go (let [response (<! (http/get url {:with-credentials? false}))]
	      (def data (map convert-to-db-data (:body response)))
	      (store data build-db-schema db-created-cont)
       )
  )
)

(defn store
  [data build-db-schema db-created-cont]
  (def schema (build-db-schema))
  (def db (-> (ds/empty-db schema) (ds/db-with data)))
  (db-created-cont db)
)


(defn build-github-url
	[repo]
	(str "https://api.github.com/repos/" repo "/issues?state=all")
)

(defn convert-github-data-to-db-data
	[x]
	{:id (:id x), :title (:title x), :state (:state x), :body (:body x), :user (:login (:user x)) :assignee (utils/none-if-nil (:assignee x) (fn[x] (:login x)))}
)

(defn build-github-db-schema
	[]
	{ 
      	  :id  {:db/axis :db.axis/none :db/card :db.card/available} 
      	  :title  {:db/axis :db.axis/none :db/card :db.card/available} 
      	  :state  {:db/axis :db.axis/available :db/card :db.card/available} 
      	  :body  {:db/axis :db.axis/none :db/card :db.card/available} 
      	  :user  {:db/axis :db.axis/available :db/card :db.card/available} 
      	  :assignee  {:db/axis :db.axis/available :db/card :db.card/available} 
   }
)

(defn build-travis-url
	[repo]
	(str "https://api.travis-ci.org/repositories/" repo "/builds")
)

(defn convert-travis-data-to-db-data
	[x]
	{:id (:id x), :repository-id (:repository_id x), :state (:state x), :branch (:branch x), :duration (:duration x), :message (:message x), :event-type (:event_type x)}
)

(defn build-travis-db-schema
	[]
	{ 
      	  :id  {:db/axis :db.axis/none :db/card :db.card/available} 
      	  :repository-id  {:db/axis :db.axis/none :db/card :db.card/available} 
      	  :state  {:db/axis :db.axis/available :db/card :db.card/available} 
      	  :branch  {:db/axis :db.axis/available :db/card :db.card/available} 
      	  :duration  {:db/axis :db.axis/none :db/card :db.card/available} 
      	  :message  {:db/axis :db.axis/none :db/card :db.card/available} 
      	  :event-type  {:db/axis :db.axis/available :db/card :db.card/available} 
   }
)
