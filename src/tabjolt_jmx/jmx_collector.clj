(ns tabjolt-jmx.jmx-collector
  (:require [clojure.java.jmx :as jmx]
            [clojure.core.async :as async]
            [tabjolt-jmx.tsv-writer :as tsv]))


(def JMX_PORT_STORE {:vizqlserver   {:host "localhost" :port 9400}
                     :dataserver    {:host "localhost" :port 10000}
                     :wgserver      {:host "localhost" :port 8300}
                     :searchservice {:host "localhost" :port 11300}
                     :vizportal     {:host "localhost" :port 8900}
                     })


;; NOTE: Threading Atttributes
(defn get-vizql-operation-names []
  (jmx/with-connection (JMX_PORT_STORE :vizqlserver)
                       (jmx/operation-names "java.lang:type=Threading")))


(defn dump-all-threads []
  (jmx/with-connection (JMX_PORT_STORE :vizqlserver)
                       (jmx/invoke-signature "java.lang:type=Threading"
                                             :dumpAllThreads ["boolean" "boolean"] true true)))


;; NOTE: RunTime Process ID
(defn get-runtime-pid []
  (jmx/with-connection (JMX_PORT_STORE :vizqlserver)
                       (jmx/read "java.lang:type=Runtime" :Name)))


(defn get-vizql-performance-metrics []
  (jmx/with-connection (JMX_PORT_STORE :vizqlserver)
                       (jmx/invoke "tableau.health.jmx:name=vizqlservice" :getPerformanceMetrics)))


;; use below functions for generalized stuff
(defn invoke-thread-information [component interface operation signature attr]
  (jmx/with-connection component
                       (jmx/invoke-signature interface operation signature attr)))


(defn collect-attribute-values [component interface attr]
  (jmx/with-connection component
                       (jmx/read interface attr)))


(defn get-all-thread-ids-for-component [component]
  (collect-attribute-values component "java.lang:type=Threading" :AllThreadIds))


(defn gather-statistics-for-component [components operations output-channel]
  (doseq [component components]
    (println "Gathering statistics for " component)
    (let [thread-ids (get-all-thread-ids-for-component (val component))]
      (doseq [thread-id thread-ids]
        (doseq [operation operations]
          (let [{:keys [interface signature]} (val operation)]
            (let [unix-timestamp (quot (System/currentTimeMillis) 1000)
                  hostname (.. java.net.InetAddress getLocalHost getHostName)
                  result (invoke-thread-information
                           (val component) interface (key operation) [signature] thread-id)
                  results-table [unix-timestamp hostname interface
                                 (clojure.string/replace (str (key operation)) ":" "")
                                 (clojure.string/replace (str (key component)) ":" "")
                                 thread-id result]
                  ]
              (async/put! output-channel results-table)
              ))))))
  (println "finished cycle.."))







