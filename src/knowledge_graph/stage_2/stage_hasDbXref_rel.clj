(ns knowledge-graph.stage-2.stage-hasDbXref-rel
  (:require
   [clojure.java.io :as io]
   [clojure.string :as str]
   [clojure.set :as set]
   [clojure.data.csv :as csv]
   [knowledge-graph.module.module :as kg]))

(defn value-by-source
  [xref-source]
  (cond
    (str/includes? xref-source "DOID") (str/replace xref-source "DOID:" "DOID_")
    (str/includes? xref-source "EFO") (str/replace xref-source "EFO:" "EFO_")
    (str/includes? xref-source "HPO") (str/replace xref-source "HPO:" "HP_")
    (str/includes? xref-source "HP") (str/replace xref-source "HP:" "HP_")
    (str/includes? xref-source "MONDO") (str/replace xref-source "MONDO:" "MONDO_")
    (str/includes? xref-source "ICD9CM") (str/replace xref-source "ICD9:" "ICD9_")
    (str/includes? xref-source "ICD9") (str/replace xref-source "ICD9CM:" "ICD9_")
    (str/includes? xref-source "ICD10CM") (str/replace xref-source "ICD10CM:" "ICD10_")
    (str/includes? xref-source "ICD10") (str/replace xref-source "ICD10CM:" "ICD10_")
    (str/includes? xref-source "ICD-10") (str/replace xref-source "ICD-10:" "ICD10_")
    (str/includes? xref-source "MeSH") (str/replace xref-source "MeSH:" "MESH_")
    (str/includes? xref-source "MSH") (str/replace xref-source "MSH:" "MESH_")
    (str/includes? xref-source "MESH") (str/replace xref-source "MESH:" "MESH_")
    (str/includes? xref-source "UMLS_CUI") (str/replace xref-source "UMLS_CUI:" "UMLS_")
    (str/includes? xref-source "UMLS") (str/replace xref-source "UMLS:" "UMLS_")
    (str/includes? xref-source "SNOMEDCT_US_2021_09_01") (str/replace xref-source "SNOMEDCT_US_2021_09_01:" "SNOMEDCT_")
    (str/includes? xref-source "SNOMEDCT_US") (str/replace xref-source "SNOMEDCT_US:" "SNOMEDCT_")
    (str/includes? xref-source "SCTID") (str/replace xref-source "SCTID:" "SNOMEDCT_")
    (str/includes? xref-source "SNOMEDCT") (str/replace xref-source "SNOMEDCT:" "SNOMEDCT_")
    (str/includes? xref-source "NCIT") (str/replace xref-source "NCIT:" "NCI_")
    (str/includes? xref-source "NCI") (str/replace xref-source "NCI:" "NCI_")
    (str/includes? xref-source "Orphanet") (str/replace xref-source "Orphanet:" "ORPHANET_")
    (str/includes? xref-source "MedDRA") (str/replace xref-source "MedDRA:" "MEDDRA_")
    :else xref-source
  ))

(defn construct_by_source
  [source source_id]
  (cond
    (str/includes? (str/lower-case source) "msh") (str/join "_" ["MESH" source_id])
    (str/includes? (str/lower-case source) "mesh") (str/join "_" ["MESH" source_id])
    (str/includes? (str/lower-case source) "nci") (str/join "_" ["NCI" source_id])
    (str/includes? (str/lower-case source) "snomedct_us") (str/join ["SNOMEDCT_" source_id])
    (str/includes? (str/lower-case source) "icd-10") (str/join "_" ["ICD10" source_id])
    (str/includes? (str/lower-case source) "idc10") (str/join "_" ["ICD10" source_id])
    (str/includes? (str/lower-case source) "idc10cm") (str/join "_" ["ICD10" source_id])
    (str/includes? (str/lower-case source) "mthicd9") (str/join "_" ["ICD9" source_id])
    (str/includes? (str/lower-case source) "icd9cm") (str/join "_" ["ICD9" source_id])
    (str/includes? (str/lower-case source) "umls") (str/join "_" ["UMLS" source_id])
    (str/includes? (str/lower-case source) "meddra") (str/join "_" ["MEDDRA" source_id])
    (str/includes? (str/lower-case source) "mdr") (str/join "_" ["MEDDRA" source_id])
    (str/includes? (str/lower-case source) "hpo") (str/replace source_id "HP:" "HP_")
    :else source_id
    ))

(defn hasDbXref
  [file-path]
  (with-open [file (io/reader (io/resource file-path))]
    (->> (csv/read-csv file :separator \tab)
         (kg/csv->map)
         (map #(assoc % :dbXref (str/replace (:dbXref %) "." "")))
         (map #(assoc % :end (value-by-source (:dbXref %))))
         (filter #(not= (:end %) ""))
         (filter #(some? (:end %)))
         (map #(assoc % :start (last (str/split (:id %) #"/"))))
         (mapv #(select-keys % [:start :end]))
         distinct)))

(defn ic9->icd10-Xref
  [disease-file-path]
  (with-open [d-file (io/reader (io/resource disease-file-path))]
    (->> (csv/read-csv d-file :separator \tab)
         (kg/csv->map)
         (map #(assoc % :icd10cm (str/replace (:icd10cm %) "." "")))
         (map #(assoc % :icd9cm (str/replace (:icd9cm %) "." "")))
         (map #(assoc % :end (:icd10cm %)))
         (map #(assoc % :start (:icd9cm %)))
         (map #(assoc % :end (str/join ["ICD10_" (:end %)])))
         (map #(assoc % :start (str/join ["ICD9_" (:start %)])))
         (mapv #(select-keys % [:start :end]))
         distinct)))

(defn medgen-Xref
  [medgen-file-path]
  (with-open [m-file (io/reader (io/resource medgen-file-path))]
    (->> (csv/read-csv m-file :separator \tab)
         (kg/csv->map)
         (map #(assoc % :source_Id (str/replace (:source_id %) "." "")))
         (map #(assoc % :end (cond 
            (str/includes? (str/lower-case (:source %)) "mesh") (str/join ["MESH_" (:source_id %)])
            (str/includes? (str/lower-case (:source %)) "snomedct_us") (str/join ["SNOMEDCT_" (:source_id %)])
            (str/includes? (str/lower-case (:source %)) "hpo") (str/replace (:source_id %) "HP:" "HP_")
            (str/includes? (str/lower-case (:source %)) "mondo") (str/replace (:source_id %) "MONDO:" "MONDO_")
            (str/includes? (str/lower-case (:source %)) "orphanet") (str/replace (:source_id %) "Orphanet_" "ORPHANET_"))))
         (map #(assoc % :start (str/join ["MEDGEN_" (:medgen_id %)])))
         (mapv #(select-keys % [:start :end]))
         distinct)))

(defn ncit-Xref
  [ncit-meddra-file-path ncit-neoplasm-file-path]
  (let [ncit->meddra (->> (csv/read-csv (io/reader (io/resource ncit-meddra-file-path)) :separator \tab)
                          (kg/csv->map)
                          (map #(assoc % :start (str/join ["NCI_" (:ncit_id %)])))
                          (map #(assoc % :end (str/join ["MEDDRA_" (:meddra_code %)])))
                          (mapv #(select-keys % [:start :end])))
        ncit->umls (->> (csv/read-csv (io/reader (io/resource ncit-neoplasm-file-path)) :separator \tab)
                        (kg/csv->map)
                        (map #(assoc % :start (str/join ["NCI_" (:ncit_id %)])))
                        (map #(assoc % :end (str/join ["UMLS_" (:ncim_cui %)])))
                        (mapv #(select-keys % [:start :end])))
        ncit->neoplasm (->> (csv/read-csv (io/reader (io/resource ncit-neoplasm-file-path)) :separator \tab)
                            (kg/csv->map)
                            (map #(assoc % :start (str/join ["NCI_" (:ncit_id %)])))
                            (map #(assoc % :end (construct_by_source (:ncim_source %) (:source_code %))))
                            (mapv #(select-keys % [:start :end])))]
        (->> (concat ncit->meddra ncit->neoplasm ncit->umls)
             distinct)))

(defn snomed<->icd10-Xref
  [snomed-icd10-file-path]
  (with-open [s-file (io/reader (io/resource snomed-icd10-file-path))]
    (let [data (csv/read-csv s-file :separator \tab)]
      (->> (kg/csv->map data)
           (map #(assoc % :icd10 (str/replace (:icd10 %) "." "")))
           (map #(assoc % :start (str/join "_" ["SNOMEDCT" (:snomed_id %)])))
           (map #(assoc % :end (str/join "_" ["ICD10" (:icd10 %)])))
           (mapv #(select-keys % [:start :end]))
           distinct))))

(defn umls-Xref
  [umls-file-path]
  (with-open [u-file (io/reader (io/resource umls-file-path))]
    (let [data (csv/read-csv u-file :separator \tab)]
      (->> (kg/csv->map data)
           (map #(assoc % :ref_id (str/replace (:ref_id %) "." "")))
           (map #(assoc % :start (str/join "_" ["UMLS" (:cuid %)])))
           (map #(assoc % :end (construct_by_source (:ref_source %) (:ref_id %))))
           (mapv #(select-keys % [:start :end]))
           distinct))))

(defn disease-nodes
  [disease-file-path]
  (with-open [d-file (io/reader (io/resource disease-file-path))]
    (->> (csv/read-csv d-file :separator \tab)
         (kg/csv->map)
         (mapv #(select-keys % [:id]))
         distinct)))

(defn run []
  (let [doid-dbXref (hasDbXref "stage_0_outputs/doid.csv")
        efo-dbXref (hasDbXref "stage_0_outputs/efo.csv")
        hpo-dbXref (hasDbXref "stage_0_outputs/hpo.csv")
        mondo-dbXref (hasDbXref "stage_0_outputs/mondo.csv")
        orphanet-dbXref (->> (hasDbXref "stage_0_outputs/orphanet.csv")
                             (mapv #(assoc % :start (str/upper-case (:start %)))))
        icd9<->icd10 (ic9->icd10-Xref "stage_0_outputs/icd9_icd10_mapping.csv")
        medgen-dbXref (medgen-Xref "stage_0_outputs/medgen_id_mapping.csv")
        ncit-dbXref (ncit-Xref "stage_0_outputs/ncit_meddra_mapping.csv" "stage_0_outputs/ncit_neoplasm_mapping.csv")
        snomed<->icd10 (snomed<->icd10-Xref "stage_0_outputs/snomedct_icd10.csv")
        umls-dbXref (umls-Xref "stage_0_outputs/umls.csv")
        dbXref (distinct (concat doid-dbXref efo-dbXref hpo-dbXref mondo-dbXref icd9<->icd10 
                                 medgen-dbXref ncit-dbXref orphanet-dbXref snomed<->icd10 umls-dbXref))
        disease (disease-nodes "stage_1_outputs/disease_nodes.csv")
        cleaned-dbXref (-> (kg/joiner dbXref disease :end :id kg/inner-join)
                           (kg/joiner disease :start :id kg/inner-join))
        reversed-cleaned-dbXref(->> (map #(set/rename-keys % {:start :end :end :start}) cleaned-dbXref))]
        (->> (concat cleaned-dbXref reversed-cleaned-dbXref)
             distinct
             (filter #(not= (:start %) (:end %)))
             (map #(assoc % :type "hasDbXref"))
             (map #(set/rename-keys % {:start :start_id :end :end_id}))
             (mapv #(select-keys % [:start_id :type :end_id]))
             (kg/write-csv [:start_id :type :end_id] "./resources/stage_2_outputs/hasDbXref_rel.csv"))))