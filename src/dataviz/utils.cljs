(ns dataviz.utils)
(defn none-if-nil [v projector]
	(if (nil? v) "none" (projector v))
)