(ns tabjolt-jmx.tsv-writer
  (:require [clojure-csv.core :as csv]))


(defn write-table-to-tsv [table output-file]
  (spit output-file (str (clojure.string/join "\t" table) "\n") :append true ) )



