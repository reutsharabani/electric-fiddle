(ns hello-fiddle.fiddles
  (:require
   [hyperfiddle.electric :as e]
   [hyperfiddle.electric-ui4 :as ui]
   [hyperfiddle.electric-dom2 :as dom]))

#?(:clj (defonce !counter (atom 0))) 
(e/def counter (e/server (e/watch !counter)))

#?(:cljs (defonce !my-counter (atom 0))) 
(e/def my-counter (e/client (e/watch !my-counter)))


(e/defn Counter []
  (e/client
   (dom/div
    (dom/h1 (dom/text "Hello world!"))
    (ui/button (e/fn []
                (swap! !my-counter inc)
                (e/server (swap! !counter inc)))
               (dom/text (str "My count: " my-counter "| Total count: " counter)))
    (dom/br)
    (ui/button (e/fn []
                (reset! !my-counter 0))
               (dom/text "reset me"))
    (dom/br)
    (ui/button (e/fn []
                (e/server (reset! !counter 0)))
               (dom/text "reset all"))
    )))


;; Dev entrypoint
;; Entries will be listed on the dev index page (http://localhost:8080)
(e/def fiddles {`Counter Counter})

;; Prod entrypoint, called by `prod.clj`
(e/defn FiddleMain [ring-request]
 (e/server
  (binding [e/http-request ring-request] ; make ring request available through the app
   (e/client
    (binding [dom/node js/document.body] ; where to mount dom elements
     (Counter.))))))
