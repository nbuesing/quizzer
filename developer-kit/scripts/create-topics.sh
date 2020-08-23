#!/bin/bash

PARTITIONS=10
REPLICATION_FACTOR=3
BOOTSTRAP_SERVER=localhost:19092


function create() {
 
   TOPIC=$1
   shift

   CLEANUP_POLICY=$1
   shift

   echo "Creating topic=$TOPIC, cleanup.policy=$CLEANUP_POLICY"
   kafka-topics \
	   --bootstrap-server $BOOTSTRAP_SERVER \
	   --create \
	   --partitions $PARTITIONS \
	   --replication-factor $REPLICATION_FACTOR \
           --config cleanup.policy=$CLEANUP_POLICY \
	   --topic $TOPIC 2>/dev/null 
}


declare -a TOPICS=( "quiz_start" "quiz_submission" "quiz_next" "quiz_result" "quiz_status" ) 
for TOPIC in ${TOPICS[@]}; do
  create $TOPIC delete &
done

declare -a COMPACTED_TOPICS=( "users" "questions" "questions_by_difficulty" )
for TOPIC in ${COMPACTED_TOPICS[@]}; do
  create $TOPIC compact &
done

wait

