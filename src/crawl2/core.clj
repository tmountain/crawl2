(ns crawl2.core
  (:use [crawl2 handler parser])
  (:require [itsy.core :refer :all]
            [itsy.extract :refer [html->str]]))

(def domain-list (tokenize (get-domains-to-process)))

(def crawl-config
  { :handler my-handler
    :workers 5
    :url-limit 10
    :url-extractor extract-all
    :host-limit true
    :polite? false })

(defn crawl-domain [domain config]
    (let [domain-config (assoc config :url domain)
          crawler (crawl domain-config)]
      (set-domain-processed domain)
      (println (str "Finished: " domain)
      (stop-workers crawler))))

(defn crawl-bucket [bucket]
  (doall (map #(crawl-domain (str "http://" %1) crawl-config) bucket)))

(def num-agents 1)

(def work-buckets (partition-all (int (/ (count domain-list)
                                       num-agents)) domain-list))

; returns a collection of agents for a given set of work buckets
(defn spawn-agents [agents buckets]
  (if (empty? buckets)
    agents
    (recur (conj agents (agent (first buckets)))
         (rest buckets))))

(def agents (spawn-agents [] work-buckets))

(defn -main [& args]
  (doseq [agent agents]
    (send-off agent crawl-bucket))
  (apply await agents)
  (shutdown-agents))
