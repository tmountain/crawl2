(ns crawl2.handler
  (:use [crawl2 parser]
        [clojurewerkz.urly.core])
  (:require [clojure.java.jdbc :as sql]
            [itsy.extract :refer [html->str]]))

(def positive-phrases (tokenize (get-file "positive_phrases.txt")))
(def negative-phrases (tokenize (get-file "negative_phrases.txt")))

(def db {:classname "com.mysql.jdbc.Driver"
         :subprotocol "mysql"
         :subname "//localhost:3306/crawl"
         :user "root"
         :password ""})

(defn insert-crawl-result
  [domain url score]
  (sql/db-do-prepared db
    "INSERT INTO crawl (domain, url, score)
       VALUES (?, ?, ?)
     ON DUPLICATE KEY UPDATE
       score = ?" [domain url score score]))

(defn get-domains-to-process
  []
  (map :domain (sql/query db ["SELECT domain FROM domain WHERE processed = 0"])))

(defn set-domain-processed
  [domain]
  (sql/db-do-prepared db
    "UPDATE domain
       SET processed = 1
     WHERE domain = ?" [domain]))

(defn my-handler [{:keys [url body]}]
  (let [domain (.getHost (url-like url))
        doc-body (normalize-str (html->str body))
        pos-score (score positive-phrases doc-body)
        neg-score (score negative-phrases doc-body)
        tot-score (- pos-score neg-score)]
     (insert-crawl-result domain url tot-score)))
