(ns webdriver.api
  (:require [clj-http.client :as client]
            [clojure.string :as str]
            [clojure.data.codec.base64 :as b64]
            [clojure.java.io :as io]))

(def default-api-params
  {:as :json
   :accept :json
   :content-type :json
   :form-params {}
   :throw-exceptions true
   ;; :debug true
   })

(def default-capabilities
  {:browserName "firefox"
   :javascriptEnabled true
   :acceptSslCerts true
   :platform "ANY"
   :marionette true
   :name "Sample Test"})

;;
;; tools
;;

(defn url-item-str [item]
  (cond
    (keyword? item) (name item)
    (string? item) item
    :else (str item)))

(defn get-url-path [items]
  (str/join "/" (map url-item-str items)))

(defn session-id [session]
  (-> session :sessionId))

(defn text-to-array [text]
  (cond
    (char? text) [text]
    :else (vec text)))

;;
;; api
;;

(defn api
  ([server method path-args]
   (api server method path-args {}))
  ([server method path-args payload]
   (let [path (get-url-path path-args)
         url (-> server :url (str "/" path))
         params (merge default-api-params
                       {:url url
                        :method method
                        :form-params payload})]
     (-> params
         client/request
         :body))))

(defn new-session [server capabilities]
  (api server :post [:session]
       {:desiredCapabilities (merge default-capabilities
                                    capabilities)}))

(defn delete-session [server session]
  (api server
       :delete
       [:session (session-id session)]))

(defn status [server]
  (api server :get [:status]))

(defn go-url [server session url]
  (api server
       :post
       [:session (session-id session) :url]
       {:url url}))

(defn go-back [server session]
  (api server
       :post
       [:session (session-id session) :back]))

(defn go-fwd [server session]
  (api server
       :post
       [:session (session-id session) :forward]))

(defn get-url [server session]
  (api server
       :get
       [:session (session-id session) :url]))

(defn get-title [server session]
  (-> server
      (api :get [:session (session-id session) :title])
      :value))

(defn element-attribute [server session element attribute]
  (-> server
      (api :get
           [:session
            (session-id session)
            :element
            element
            :attribute
            attribute])
      :value))

(defn find-element [server session [locator term]]
  (-> server
      (api :post
           [:session (session-id session) :element]
           {:using locator :value term})
      :value
      first
      second))

(defn element-find [server session [locator term]]
  (-> server
      (api :post
           [:session (session-id session) :element]
           {:using locator :value term})
      :value
      first
      second))

(defn element-tag-name [server session element]
  (-> server
      (api :get
           [:session (session-id session) :element element :name])
      :value))

(defn element-enabled [server session element]
  (-> server
      (api :get
           [:session (session-id session) :element element :enabled])
      :value))

(defn find-element-from-element [server session element selector]
  (-> server
      (api :post
           [:session (session-id session) :element element]
           {:using "xpath" :value "test"})
      :value
      first
      second))

(defn element-value [server session element text]
  (-> server
      (api :post
           [:session (session-id session) :element element :value]
           {:value (text-to-array text)})))

;; (defn element-click [browser element]
;;   ;; (api browser
;;   ;;      :post
;;   ;;      [:session (-> browser :session :sessionId) :element element :click])
;;   )


;; (def url-element-click! "/session/%s/element/%s/click")


;; (def url-session "/session")
;; (def url-session-delete "/session/%s")
;; (def url-go-url "/session/%s/url")
;; (def url-go-back "/session/%s/back")
;; (def url-go-forward "/session/%s/forward")
;; (def url-get-title "/session/%s/title")
;; (def url-get-url "/session/%s/url")
;; (def url-get-cookie "/session/%s/cookie")
;; (def url-get-cookie-by-name "/session/%s/cookie/%s")
;; (def url-get-active-element "/session/%s/element/active")
;; (def url-element-selected? "/session/%s/element/%s/selected")
;; (def url-get-element-attr "/session/%s/element/%s/attribute/%s")
;; (def url-get-element-prop "/session/%s/element/%s/property/%s")
;; (def url-get-element-text "/session/%s/element/%s/text")
;; (def url-get-element-name "/session/%s/element/%s/name")
;; (def url-element-enabled? "/session/%s/element/%s/enabled")
;; (def url-find-element "/session/%s/element")
;; (def url-find-elements "/session/%s/elements")
;; (def url-element-click! "/session/%s/element/%s/click")
;; (def url-element-clear! "/session/%s/element/%s/clear")
;; (def url-element-value! "/session/%s/element/%s/value")
;; (def url-execute-script-sync! "/session/%s/execute/sync")
;; (def url-screenshot "/session/%s/screenshot")

;; /session/{session id}/element/{element id}/element


;; (defn url-item [item]
;;   (cond
;;     (keyword? item) (name item)
;;     (string? item) item
;;     :else (str item)))

;; (defn get-path [& args]
;;   (str/join "/" (map url-item args)))

;; (defn get-status [server]
;;   (-> server
;;       (str "/" (get-path :status))
;;       (client/get params)
;;       :body
;;       :value))

;; (defn get-session [server opt]
;;   (-> server
;;       (str "/" (get-path :session))
;;       (client/post
;;        (assoc params :form-params opt))
;;       :body)) ;; todo erro

;; (defn delete-session [server session]
;;   (-> server
;;       (str "/" (get-path :session (:sessionId session)))
;;       (client/delete params)
;;       :body
;;       :value))

;; (defn go-url [server session url]
;;   (-> server
;;       (str "/" (get-path :session
;;                          (:sessionId session)
;;                          :url))
;;       (client/post
;;        (assoc params :form-params {:url url}))
;;       :body))

;; (defn go-back [session]
;;   (-> (str url-server
;;            (format url-go-back (:sessionId session)))
;;       (client/post params)
;;       :body))

;; (defn go-forward [session]
;;   (-> (str url-server
;;            (format url-go-forward (:sessionId session)))
;;       (client/post params)
;;       :body))

;; (defn get-title [session]
;;   (-> (str url-server
;;            (format url-get-title (:sessionId session)))
;;       (client/get params)
;;       :body
;;       :value))

;; (defn get-cookie [session]
;;   (-> (str url-server
;;            (format url-get-cookie (:sessionId session)))
;;       (client/get params)
;;       :body
;;       :value))

;; (defn get-cookie-by-name [session name]
;;   (-> (str url-server
;;            (format url-get-cookie-by-name
;;                    (:sessionId session)
;;                    name))
;;       (client/get params)
;;       :body
;;       :value
;;       first))

;; (defn get-active-element [session]
;;   (-> (str url-server
;;            (format url-get-active-element
;;                    (:sessionId session)))
;;       (client/get params)
;;       :body
;;       :value
;;       first
;;       second))

;; (defn element-selected? [session element]
;;   (-> (str url-server
;;            (format url-element-selected?
;;                    (:sessionId session)
;;                    element))
;;       (client/get params)
;;       :body
;;       :value))

;; (defn get-element-attr [session element attr]
;;   (-> (str url-server
;;            (format url-get-element-attr
;;                    (:sessionId session)
;;                    element
;;                    attr))
;;       (client/get params)
;;       :body
;;       :value))

;; (defn get-element-prop [session element prop]
;;   (-> (str url-server
;;            (format url-get-element-prop
;;                    (:sessionId session)
;;                    element
;;                    prop))
;;       (client/get params)
;;       :body
;;       :value))

;; (defn get-element-text [session element]
;;   (-> (str url-server
;;            (format url-get-element-text
;;                    (:sessionId session)
;;                    element))
;;       (client/get params)
;;       :body
;;       :value))

;; (defn get-element-name [session element]
;;   (-> (str url-server
;;            (format url-get-element-name
;;                    (:sessionId session)
;;                    element))
;;       (client/get params)
;;       :body
;;       :value))

;; (defn element-enabled? [session element]
;;   (-> (str url-server
;;            (format url-element-enabled?
;;                    (:sessionId session)
;;                    element))
;;       (client/get params)
;;       :body
;;       :value))

;; (defn element-click! [session element]
;;   (-> (str url-server
;;            (format url-element-click!
;;                    (:sessionId session)
;;                    element))
;;       (client/post params)
;;       :body))

;; (defn element-clear! [session element]
;;   (-> (str url-server
;;            (format url-element-clear!
;;                    (:sessionId session)
;;                    element))
;;       (client/post params)
;;       :body))

;; (defn element-value! [session element text]
;;   (-> (str url-server
;;            (format url-element-value!
;;                    (:sessionId session)
;;                    element))
;;       (client/post
;;        (assoc params :form-params {:value (vec text)}))
;;       :body))

;; (defn find-element [session locator selector]
;;   (-> (str url-server
;;            (format url-find-element
;;                    (:sessionId session)))
;;       (client/post
;;        (assoc params :form-params {:using locator :value selector}))
;;       :body
;;       :value
;;       first
;;       second
;;       )) ;; todo

;; (defn find-elements [session locator selector]
;;   (-> (str url-server
;;            (format url-find-elements
;;                    (:sessionId session)))
;;       (client/post
;;        (assoc params :form-params {:using locator :value selector}))
;;       :body
;;       :value ;; todo keys


;;       ))

;; (defn execute-script-sync! [session script & args]
;;   (-> (str url-server
;;            (format url-execute-script-sync!
;;                    (:sessionId session)))
;;       (client/post
;;        (assoc params :form-params {:script script :args args}))
;;       :body
;;       :value))

;; (defn inject-script! [session url]
;;   (let [script (str "var s = document.createElement('script');"
;;                     "s.type = 'text/javascript';"
;;                     "s.src = arguments[0];"
;;                     "document.head.appendChild(s);")]
;;     (execute-script-sync! session script url)))

;; (defn get-url [session]
;;   (-> (str url-server
;;            (format url-get-url
;;                    (:sessionId session)))
;;       (client/get params)
;;       :body
;;       :value))

;; (defn get-screenshot [session]
;;   (-> (str url-server
;;            (format url-screenshot
;;                    (:sessionId session)))
;;       (client/get params)
;;       :body
;;       :value
;;       .getBytes
;;       b64/decode
;;       ;; byte-array
;;       (io/copy "foo.png")
;; ))
