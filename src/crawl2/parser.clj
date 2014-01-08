(ns crawl2.parser
  (:require [clojure.java.io :as io])
  (:use [clojure.string :only [replace trim split lower-case] :rename {replace str-replace}]))

(defn get-file [file]
  (slurp (-> file io/resource io/file)))

(defn match-freq [phrase string]
  (->>
    (map #(clojure.string/split %1 #" ")
         (re-seq (re-pattern phrase) (lower-case string)))
    (map count)))

(defn score [terms string]
  (let [matches (map #(match-freq %1 string) terms)]
    (reduce + (flatten matches))))

(defn dump-match-freq [phrase string]
    (map #(clojure.string/split %1 #" ")
         (re-seq (re-pattern phrase) (lower-case string))))

(defn raw-score [terms string]
    (filter #(not (empty? %1)) 
        (map #(dump-match-freq %1 string) terms)))

(defn tokenize [string]
  (let [tokens (split string #"\n")]
    (map lower-case tokens)))

(defn normalize-str [string]
  (let [string (str-replace string #"\W" " ")]
    (str-replace (trim string) #"\s+" " ")))
