(ns dataviz.core
  (:require
    [dataviz.ui :as ui]
    [dataviz.connector :as c]
    [datascript.core :as data])
  )

(enable-console-print!)

(defn open-board
  [source-type repo]
    (defn make-slice [db, x, y]
    	(def s (:schema db))
        (defn axis [a]
          (if (= a "none") 
            `()
            (map first
              (data/q '[:find ?value
                  :in $ [[[?attr [[?aprop ?avalue] ...]] ...] ?t]
                  :where [(= ?attr ?t)]
                  [?entity ?attr ?value]]
              db [s (keyword a)]))))

        (defn cells [x y]
        	(if (= c "none") 
	        	  `()
	        	  (let [cell-db-data (data/q '[:find ?entity ?attr ?value
					                :in $ [[[?attr [[?aprop ?avalue] ...]] ...] ?x ?y]
					                :where [(or (= ?avalue :db.card/available) (= ?attr ?x) (= ?attr ?y))]
					                [?entity ?attr ?value]]
					                db [s (keyword x) (keyword y)])]
	        	   (def grouped (group-by (fn[x] (first x)) cell-db-data))
	        	   (def raw-cells-data (map (fn[x] 
	        	   	   (reduce (fn[acc x]
	        	   	   		 		(let [[id attr value] x]
	        	   	   		 			(merge acc {attr value})
	        	   	   		 		)
	        	   	   		   )
	        	   	   		   {}
	        	   	   		   (rest x)
	        	   	   )
	        	   	) (vals grouped)))
	        	   (def cells-data (map (fn[c]
	        	   							{
	        	   								:x (get c (keyword x))
	        	   								:y (get c (keyword y))
	        	   								:data (dissoc c (keyword x) (keyword y))
	        	   							}
	        	   						)
	        	   						raw-cells-data
	        	   					)
	        	   )
				   cells-data
	        	  )
       		)
        )
        (def xaxis (axis x))
        (def yaxis (axis y))
        (def cells (cells x y))

        {:xaxis {:id x :values xaxis} :yaxis {:id y :values yaxis} :cells cells}
      )

    (defn prepare-attr [db]
      (map name (keys
        (filter
          (fn [[k v]]
              (= (:db/axis v) :db.axis/available))
          (:schema db)))))

    (c/import repo source-type (fn[db]
        (def schema (prepare-attr db))
        (def mk (partial make-slice db))
        (defn update [x y]
            (def data (mk x y))
            (ui/render-board schema data update)
          )
        (update (first schema) (last schema))
      ) rep)
    )

(defn ^:export start
  []
    (ui/render-home (fn [type rep]
        (open-board (keyword type) rep)
      ))
  )
