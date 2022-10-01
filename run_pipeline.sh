#!bin/zsh
echo "PREPROCESSING UMLS DATA"
python3 ./scripts/process_umls.py

echo "###################################"
echo "STARTING STAGE 0"                 
echo "###################################"

clj -M:stage-0                  

echo "###################################"
echo "STAGE 0 FINISHED"                 
echo "###################################"

echo "\n"

echo "###################################"
echo "STARTING STAGE 1"                 
echo "###################################"

clj -M:stage-1

echo "###################################"
echo "STAGE 1 FINISHED"                 
echo "###################################"

echo "\n"

echo "###################################"
echo "STARTING STAGE 2"                 
echo "###################################"

clj -M:stage-2

echo "###################################"
echo "STAGE 2 FINISHED"                 
echo "###################################"