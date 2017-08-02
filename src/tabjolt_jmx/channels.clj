(ns tabjolt-jmx.channels
  (:require [clojure.core.async :as async]
            [tabjolt-jmx.jmx-collector :as collector]
            [tabjolt-jmx.tsv-writer :as tsv]))


(defn jmx-transfer-machine [input timeout-ms output-file]
  (let [end-of-execution (async/timeout timeout-ms)]
    (async/go-loop [print-count 0]
      (let [[v c] (async/alts! [end-of-execution input])]
        (cond
          (= c end-of-execution) (println "Done collecting statistics")
          (= c input) (do
                        (println v)
                        (tsv/write-table-to-tsv v output-file)
                        (recur (inc print-count))))))))




