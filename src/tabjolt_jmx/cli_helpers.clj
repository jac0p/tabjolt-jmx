(ns tabjolt-jmx.cli-helpers
  (:require [clojure.tools.cli :as cli]))


(defn show-usage [summary]
  (->> ["For usage please refer to the following options:"
        summary
        ""]
       (clojure.string/join \newline)))


(defn show-errors [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (clojure.string/join \newline errors)))


(defn exit [status msg]
  (println msg)
  (System/exit status))


(defn verify-cli-options [cli-options]
  (let [{:keys [options arguments errors summary]} cli-options]
    (cond
      (:help options) (exit 0 (show-usage summary))
      (= (count options) 0) (exit 1 (show-usage summary))
      errors (exit 1 (show-errors errors)))))


(def cli-options
  [["-d" "--duration DURATION" "Duration of JMX statistic gathering in seconds"
    ;:default 60
    :parse-fn #(Long/parseLong %)]
   ["-f" "--frequency FREQ" "Defines how often the statistic gathering should run in seconds"
    ;:default 15
    :parse-fn #(Long/parseLong %)]
   ["-h" "--help"]])
