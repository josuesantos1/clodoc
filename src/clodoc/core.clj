(ns clodoc.core
  (:gen-class)
  (:require [telegrambot-lib.core :as tbot]
            [clojure.string :as str])
  (:use [clojure.repl]))

(def bot (tbot/create "2145251531:AAF-PdAPvKHXcxQT5DfqaMPP8ALV_P2fy9k"))

(def offset (atom nil))
(def doc-cache (atom nil))
(def msgs-id (atom nil))

(def link "https://t.me/share/url?url=clodocbot")

(defn start
  [chat-id]
  (tbot/send-message bot chat-id ""))

(defn send-command
  [msg chat-id]
  (let [msg-splited (str/split msg #" ")
        cmd (get msg-splited 1)]
    (println "|---send command---|")
    (reset! doc-cache (with-out-str (doc cmd)))
    (tbot/send-message bot chat-id cmd)))

(defn link-command
  [msg chat-id]
  (let [msg-splited (str/split msg #" ")
        cmd (get msg-splited 1)]
    (tbot/send-message bot chat-id (str link "&text=" cmd))))

(defn get-updates
  []
  (let [result (get-in (tbot/get-updates bot) [:result])
        last (last result)
        las (get-in last [:update_id])]
    (println "|---get updates---|" )
    (reset! offset las)))

(defn which-command?
  [msg]
  (let [msg-splited (str/split msg #" ")]
    (println "|---which command?---|")
    (first msg-splited)))

(defn command
  [msg chat-id]
  (println "|---command---|")
  (cond
    ;; (= (which-command? msg) "/start") (start chat-id)
    (= (which-command? msg) "/s") (send-command msg chat-id)
    (= (which-command? msg) "/l") (tbot/send-message bot chat-id "TODO: create link")
    :else (tbot/send-message bot chat-id "are you crazy or do you want a dollar?")))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!")
  (while true
    (if (nil? @offset)
      (get-updates)
      (let [result (get-in (tbot/get-updates bot  {:offset @offset}) [:result])]
        (doseq [x result]
          (let [chat-id (get-in x [:message :chat :id])
                text (get-in x [:message :text])
                message-id (get-in x [:message :message_id])]
            (println "------" message-id "------")
            (if (>= (count @msgs-id) 10)
              (reset! msgs-id (drop-last @msgs-id))
              (do
                (println "|---add to msgs-id---|")
                (if (nil? (some #{message-id} @msgs-id))
                  (do
                    (swap! msgs-id conj message-id)
                    (command text chat-id)
                    (println @msgs-id))
                  (println "|---duplicate---|" @msgs-id))))))))))

;; (let [chat-id (get-in x [:message :chat :id])
;;       text (get-in x [:message :text])]
;;   (println "------")
;;   (command text chat-id))