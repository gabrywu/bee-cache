#!/usr/bin/env bash

SHELL_SCRIPT_PATH="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
rm "${SHELL_SCRIPT_PATH}"/distribute/lib/*
mvn clean package -DskipTests=true
