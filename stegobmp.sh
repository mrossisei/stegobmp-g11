#!/bin/bash


SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )


JAR_PATH="$SCRIPT_DIR/target/stegobmp-1.0-SNAPSHOT-jar-with-dependencies.jar"


java -jar "$JAR_PATH" "$@"