(ns knowledge-graph.stage-1.stage-efo-nodes
  (:require
   [clojure.java.io :as io]
   [clojure.data.csv :as csv]
   [clojure.string :as str]
   [knowledge-graph.module.module :as kg]))

(defn get-results
  [file-path]
  (with-open [file (io/reader (io/resource file-path))]
    (let [data (csv/read-csv file :separator \tab)]
      (->>
       (kg/csv->map data)
       (filter #(str/includes? (:id %) "EFO"))
       (map #(assoc % :id (last (str/split (:id %) #"/"))))
       (map #(assoc % :source_id (str/replace (:id %) #"_" ":")))
       (map #(assoc % :name (:label %)))
       (map #(assoc % :label "EFO"))
       (map #(assoc % :source "EFO"))
       (mapv #(select-keys % [:id :label :name :source_id :source]))
       distinct))))

(defn run []
  (let [efo (get-results "stage_0_outputs/efo.csv")]
    (kg/write-csv [:id :label :name :source_id :source] "./resources/stage_1_outputs/efo_nodes.csv" efo)))