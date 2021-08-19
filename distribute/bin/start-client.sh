#!/usr/bin/env bash
SHELL_SCRIPT_PATH="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LIB_PATH=${SHELL_SCRIPT_PATH}/../lib
export CLASSPATH=${CLASSPATH}:${LIB_PATH}/*
source $SHELL_SCRIPT_PATH/env.sh
java -classpath ${CLASSPATH}:${LIB_PATH}/bee-cache-client-1.0-SNAPSHOT.jar com.gabry.beecache.client.benchmark.BeeCacheBenchmark
