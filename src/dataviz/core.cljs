(ns dataviz.core
  (:require
    [dataviz.ui :as ui]
    [dataviz.connector :as c]
    [dataviz.setup :as setup]
    [datascript.core :as data]
    [dataviz.utils :as utils]
    [cljs.compiler :as comp]
    [cljs.analyzer :as ana]
    ;[cljs.env :as env]
    [cljs.reader :as reader]
    [cljs.pprint :as pp]
    ;[cljs.js :as js]
    )
  )

(enable-console-print!)

;(defonce st (env/default-compiler-env))

(defn- exp [sym env]
  (let [mvar
        (when-not (or (-> env :locals sym)        ;locals hide macros
                      (-> env :ns :excludes sym))
          (if-let [nstr (namespace sym)]
            (when-let [ns (cond
                           (= "clojure.core" nstr) (find-ns 'cljs.core)
                           (.contains nstr ".") (find-ns (symbol nstr))
                           :else
                           (-> env :ns :requires-macros (get (symbol nstr))))]
              (.findInternedVar ^clojure.lang.Namespace ns (symbol (name sym))))
            (if-let [nsym (-> env :ns :uses-macros sym)]
              (.findInternedVar ^clojure.lang.Namespace (find-ns nsym) sym)
              (.findInternedVar ^clojure.lang.Namespace (find-ns 'cljs.core) sym))))]
    (let [sym (symbol (name sym))]
      (when (and mvar (or (setup/clojure-macros sym) (setup/cljs-macros sym)))
        @mvar))))



(defn open-board
  [source-type repo]
    (defn make-slice [db, x, y]
    	(def s (:schema db))
        (defn axis [a]
            (let [data  (map first (data/q '[:find ?value
							                  :in $ [[[?attr [[?aprop ?avalue] ...]] ...] ?t]
							                  :where [(= ?attr ?t)]
							                  [?entity ?attr ?value]]
              						db [s (keyword a)]))
            	]
            (def vals-without-none (remove utils/none? data))
            (cons (utils/none-if-nil nil) vals-without-none)
            )
         )

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
	        	   	   		   x
	        	   	   )
	        	   	) (vals grouped)))
	        	   (def cells-data (map (fn[c]
	        	   							{
	        	   								:x (utils/none-if-nil (get c (keyword x)) identity)
	        	   								:y (utils/none-if-nil (get c (keyword y)) identity)
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


    (defn build [action locals expr]
      {:result
       (binding [ana/*cljs-ns* 'cljs.user]
         (let [env {:ns (@ana/namespaces ana/*cljs-ns*)
                    :uses #{'cljs.core}
                    :context :expr
                    :locals locals}]
           (with-redefs [ana/get-expander exp]
             (action env expr)
           )
        )
      )
      :status 200})

    (def compilation (partial build
                           #(comp/emit-str (ana/analyze % %2))
                           (setup/load-core-names)))

    (def analyze (partial build
                      #(ana/analyze % %2)
                      (setup/load-core-names)
                  )
    )


    (c/import repo source-type (fn[db]
        (def schema (prepare-attr db))
        (def mk (partial make-slice db))
        (defn add-code [code] 
          (def expr (reader/read-string code))
          (print "expression" expr)
          ;(def analyzed (analyze expr))
          ;(pp/pprint analyzed)
          (def compiled (compilation expr))
          (print "compiled")
          (pp/pprint compiled)
          (def evaled (js/eval (:result compiled) ))
          (print "evaled" evaled)
        )
        (defn update [x y]
            (def data (mk x y))
            (ui/render-board schema data update add-code)
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
