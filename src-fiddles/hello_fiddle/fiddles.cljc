(ns hello-fiddle.fiddles
  (:require
   [hyperfiddle.electric :as e]
   [hyperfiddle.electric-ui4 :as ui]
   [hyperfiddle.electric-dom2 :as dom])
  (:import
   #?(:clj
      [java.util UUID])))

#?(:clj (defonce !userid->user (atom {})))
(e/def userid->user (e/server (e/watch !userid->user)))

#?(:cljs (defonce !my-userid (atom nil)))
(e/def my-userid (e/client (e/watch !my-userid)))

#?(:cljs (defonce !my-username (atom nil)))
(e/def my-username (e/client (e/watch !my-username)))

#?(:clj
   (defn uuid []
     (str (UUID/randomUUID))))

#?(:clj
   (comment
     (deref !userid->user)
     (uuid)))

(e/defn Login []
  (e/client
    (dom/div
      (dom/h1 (dom/text "Login"))
      (dom/input
        (dom/props {:placeholder "username"})
        (dom/on "keydown" (e/fn [e]
                            (when (= "Enter" (.-key e))
                              (let [username (.. e -target -value)]
                                (when-not (empty? username)
                                  (e/server
                                    (let [new-id (uuid)]
                                      (swap! !userid->user assoc new-id {:username username
                                                                         :clicks 0
                                                                         :ready? false
                                                                         :last-click (e/-get-system-time-ms)})
                                      (e/client
                                        (reset! !my-username username)
                                        (reset! !my-userid new-id))))))))))
      (dom/br)
      (ui/button
          (e/fn []
            (e/server (reset! !userid->user {})))
        (dom/text "reset users")))))

(e/defn ScoreBoard []
  (e/client
    (dom/div (dom/h2 (dom/text "Scoreboard"))
             (ui/button (e/fn []
                          (e/server (swap! !userid->user update-in [my-userid :clicks] inc)))
               (dom/text "test"))
             (e/server (e/for-by first [[userid user] (sort-by #(-> % val :clicks (* -1)) userid->user)]
                         (let [clicks (:clicks user)
                               username (:username user)]
                           (e/client
                             (dom/h3
                               (when (= my-userid userid)
                                 (dom/style {:background-color "yellow"}))
                               (dom/text (str username ":" clicks))))))))))

(e/defn Lobby []
  (e/client
    (dom/div (dom/h2 (dom/text "Lobby"))
             (ui/button (e/fn []
                          (e/server (swap! !userid->user update-in [my-userid :ready?] not)))
               (dom/text "Ready!"))
             (e/server (e/for-by first [[userid user] userid->user]
                         (let [ready? (:ready? user)
                               username (:username user)]
                           (e/client
                             (dom/h3
                               (when (= my-userid userid)
                                 (dom/style {:background-color "yellow"}))
                               (dom/text (str username ":" (if ready?
                                                             "ready"
                                                             "waiting")))))))))))
(e/defn Game []
  (e/client
    (let [user-count (count userid->user)
          all-ready? (every? :ready? (vals userid->user))]
      (if (and all-ready? (> user-count 1))
        (ScoreBoard.)
        (Lobby.)))
    (println "game")))

(e/defn Catch []
  (e/client
    (if my-username
      (Game.)
      (Login.))))

(e/def fiddles {`Catch Catch})

(e/defn FiddleMain [ring-request]
  (e/server
    (binding [e/http-request ring-request]
      (e/client
        (binding [dom/node js/document.body]
          (Catch.))))))
