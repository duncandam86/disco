#! /bin/bash
cypher-shell -u $NEO4J_USER -p $NEO4J_PASS "create index on :DOID(source_id)"
cypher-shell -u $NEO4J_USER -p $NEO4J_PASS "create index on :EFO(source_id)"
cypher-shell -u $NEO4J_USER -p $NEO4J_PASS "create index on :HPO(source_id)"
cypher-shell -u $NEO4J_USER -p $NEO4J_PASS "create index on :ICD10CM(source_id)"
cypher-shell -u $NEO4J_USER -p $NEO4J_PASS "create index on :ICD9CM(source_id)"
cypher-shell -u $NEO4J_USER -p $NEO4J_PASS "create index on :MEDDRA(source_id)"
cypher-shell -u $NEO4J_USER -p $NEO4J_PASS "create index on :MEDGEN(source_id)"
cypher-shell -u $NEO4J_USER -p $NEO4J_PASS "create index on :MESH(source_id)"
cypher-shell -u $NEO4J_USER -p $NEO4J_PASS "create index on :MONDO(source_id)"
cypher-shell -u $NEO4J_USER -p $NEO4J_PASS "create index on :NCIT(source_id)"
cypher-shell -u $NEO4J_USER -p $NEO4J_PASS "create index on :ORPHANET(source_id)"
cypher-shell -u $NEO4J_USER -p $NEO4J_PASS "create index on :SNOMEDCT(source_id)"
cypher-shell -u $NEO4J_USER -p $NEO4J_PASS "create index on :SYNONYM(name)"
cypher-shell -u $NEO4J_USER -p $NEO4J_PASS "create index hasDbXref for ()-[r:hasDbXref]-() on r.id"