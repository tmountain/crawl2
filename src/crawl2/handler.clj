(ns crawl2.handler
  (:use [crawl2 parser]
        [clojurewerkz.urly.core])
  (:require [clojure.java.jdbc :as sql]
            [itsy.extract :refer [html->str]]))

(def db {:classname "com.mysql.jdbc.Driver"
         :subprotocol "mysql"
         :subname "//localhost:3306/crawl"
         :user "crawl"
         :password "secret"})

(defn get-positive-phrases []
  (map :keyword (sql/query db ["SELECT keyword FROM keyword WHERE bias = 'positive'"])))

(defn get-negative-phrases []
  (map :keyword (sql/query db ["SELECT keyword FROM keyword WHERE bias = 'negative'"])))

(def positive-phrases (get-positive-phrases))
(def negative-phrases (get-negative-phrases))

(defn insert-match-result
  [domain url match type]
  (sql/db-do-prepared db
    "INSERT INTO matches (domain, url, term, type)
       VALUES (?, ?, ?, ?)" [domain url match type]))

(defn insert-crawl-result
  [domain url score]
  (sql/db-do-prepared db
    "INSERT INTO crawl (domain, url, score)
       VALUES (?, ?, ?)
     ON DUPLICATE KEY UPDATE
       score = ?" [domain url score score]))


(defn get-domains-to-process
  []
  (map :domain (sql/query db ["SELECT domain FROM domain WHERE processed = 0 LIMIT 250"])))

(defn set-domain-processed
  [domain]
  (sql/db-do-prepared db
    "UPDATE domain
       SET processed = 1
     WHERE domain = ?" [domain]))

(defn save-each-match
  [domain url matches match-type]
    (doseq [match matches]
      (doseq [term match]
        (insert-match-result domain url (clojure.string/join " " term) match-type))))

(defn my-handler [{:keys [url body]}]
  (let [domain (.getHost (url-like url))
        doc-body (normalize-str (html->str body))
        pos-score (score positive-phrases doc-body)
        neg-score (score negative-phrases doc-body)
        raw-pos-score (raw-score positive-phrases doc-body) 
        raw-neg-score (raw-score negative-phrases doc-body)
        tot-score (- pos-score neg-score)]
     (save-each-match domain url raw-pos-score "positive")
     (save-each-match domain url raw-neg-score "negative")
     (insert-crawl-result domain url tot-score)))
