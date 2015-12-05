(ns dataviz.core
  (:require
    [clojure.string :as str]
    [cemerick.url :as u]
    [dataviz.ui :as ui]
    [dataviz.connector :as c]
    [cljs.core.async :as async]
    [datascript.core :as data]
    [quiescent.core :as q :include-macros true]
    [quiescent.dom :as dom]))

(enable-console-print!)

; Many thanks to http://leonid.shevtsov.me/en/oauth2-is-easy
 (def oauth2-params
   {:client-id "05ed84dcbf548234924a"
    :client-secret "a4356b5a8c532a6f52edf6df9f191b1ddea7cb42"
    :authorize-uri  "https://github.com/login/oauth/authorize"
    :redirect-uri "http://localhost"
    :access-token-uri "https://github.com/login/oauth/access_token"
    :scope "public_repo"}) 

(defn authorize-uri [client-params csrf-token]
   (str
     (:authorize-uri client-params)
     "?response_type=code"
     "&client_id="
     (u/url-encode (:client-id client-params))
     "&redirect_uri="
     (u/url-encode (:redirect-uri client-params))
     "&scope="
     (u/url-encode (:scope client-params))))

(defn get-authentication-response [csrf-token response-params]
   (if (= csrf-token (:state response-params))
     (try
       (-> (http/post (:access-token-uri oauth2-params)
                      {:form-params {:code         (:code response-params)
                                     :grant_type   "authorization_code"
                                     :client_id    (:client-id oauth2-params)
                                     :redirect_uri (:redirect-uri oauth2-params)}
                       :basic-auth [(:client-id oauth2-params) (:client-secret oauth2-params)]
                       :as          :json
                       })
           :body)
       (catch Exception _ nil))
     nil))


(q/defcomponent SignIn 
  []
  (dom/div {}
    (dom/button {:onClick (fn [_]
                          (.replace js/location (authorize-uri oauth2-params)))}
      "Sign in with GitHub")))

(defn open-signin []
    (q/render (SignIn) js/document.body)
  )

(defn open-home [auth-code]
    (println auth-code)
    (c/import)
    (ui/render)
  )

(defn ^:export start 
  []
    ; (def auth-code (get 
    ;     (:query (u/url (-> js/window .-location .-href)))
    ;     "code"))
    ; (if (nil? auth-code) (open-signin) (open-home auth-code))
    (c/import)
    (ui/render)
  )
