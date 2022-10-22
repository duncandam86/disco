#!/bin/zsh
echo "DOWNLOAD ... "

source ./scripts/download.sh

echo "FINISH DOWNLOADING!"

echo "RUN PIPELINE ..."

source ./scripts/run_pipeline.sh

echo "FINISH RUNNING PIPELINE!"

echo "BUILDING IMAGE"
docker-compose build

echo "SPINNING UP CONTAINER"
docker-compose up -d

echo "GET DOCKER ID"
CONTAINER_ID="$(docker ps -aqf 'name=^disease-mapping')"
echo $CONTAINER_ID

echo "LOAD GRAPH"
docker exec $CONTAINER_ID bin/neo4j-admin import --database=dbdiseasemapping  --delimiter TAB  --high-io=true --skip-duplicate-nodes=true --multiline-fields=true --force \
--nodes='import/nodes.csv' \
--relationships='import/relationships.csv'

echo "RESTARTING CONTAINER WITH NEW GRAPH"
docker-compose restart

sleep 30

echo "SET INDEX"
docker exec $CONTAINER_ID bin/cypher-shell -u $NEO4J_USER -p $NEO4J_PASS 'create index on :MODEL(model)'