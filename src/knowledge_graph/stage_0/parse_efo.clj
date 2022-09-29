(ns knowledge-graph.stage-0.parse-efo
  (:require
   [clj-http.client :as client]
   [clojure.data.xml :as d-xml]
   [clojure.data.zip.xml :refer [attr text xml-> xml1->]]
   [clojure.string :as str]
   [clojure.zip :as z]
   [knowledge-graph.module.module :as kg]))

(defn class-map
  "Extract information from xml elements and create a map"
  [data]
  (let [z (z/xml-zip data)]
    (for [id             (xml-> z :Class (attr :rdf/about))
          label          (xml-> z :Class :label text)
          subClassOf     (xml-> z :Class :subClassOf (attr :rdf/resource))
          dbXref         (xml-> z :Class :hasDbXref text)
          synonym        (concat (xml-> z :Class :hasExactSynonym text) (xml-> z :Class :hasRelatedSynonym text))
          is_disease     (xml-> z :Class :inSubset (attr :rdf/resource))]
          {:id id :label label :subClassOf subClassOf :dbXref dbXref :synonym synonym :is_disease is_disease})))

(defn get-results
  "Download xml file, parse for necessary information, and write as csv output"
  [url output_path]
  (->> (client/get url {:as :stream})
       :body
       (d-xml/parse)
       :content
       (filter #(= (:tag %) :Class))
       (map class-map)
       (apply concat)
       (filter #(some? (:id %)))
       (filter #(or (str/includes? (:is_disease %) "disease") (str/includes? (:is_disease %) "disorder") (str/includes? (:is_disease %) "syndrome")))
       distinct
       (kg/write-csv [:id :label :subClassOf :dbXref :synonym] output_path)))

(defn run []
  (let [url "https://github.com/EBISPOT/efo/releases/download/current/efo.owl"
        output "./resources/stage_0_outputs/efo.csv"]
    (get-results url output)))

