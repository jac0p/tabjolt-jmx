(ns tabjolt-jmx.core
  (:require [clojure.edn :as edn]
            [clojure.tools.cli :as cli-tools]
            [overtone.at-at :as at-at]
            [clojure.core.async :as async]
            [tabjolt-jmx.jmx-collector :as collector]
            [tabjolt-jmx.tsv-writer :as tsv]
            [tabjolt-jmx.cli-helpers :as cli-helpers]
            [tabjolt-jmx.channels :as channels])
  (:gen-class))



(defn -main [& args]
  (let [opts (cli-tools/parse-opts args cli-helpers/cli-options)]

    (println "Verifying CLI parameters...")
    (cli-helpers/verify-cli-options opts)

    (println "No problems found with CLI parameters..")

    (let [counter-config (edn/read-string (slurp "config/jmxcounters.edn"))
          components (counter-config :components)
          operations (counter-config :threading-counters)
          output-file (str "data/jmx-threading-results.tsv")]

      (println "Starting stats gathering")


      (let [{:keys [duration frequency]} (opts :options)
            output-channel (async/chan 10)]

        (channels/jmx-transfer-machine output-channel duration output-file)
        (while true
          (doall
            (collector/gather-statistics-for-component components operations output-channel)
            (Thread/sleep (* frequency 1000))
          ))))))
