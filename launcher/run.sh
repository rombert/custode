#!/bin/sh -e

launcher_ver=0.8.0
launcher_jar=${HOME}/.m2/repository/org/apache/sling/org.apache.sling.feature.launcher/${launcher_ver}/org.apache.sling.feature.launcher-${launcher_ver}.jar

rebuild="0"
clean="0"
feature_files="src/main/features/app.json"

usage() {
    echo "Custode dev launcher"
    echo ""
	echo "Usage: $0 [-a first.json,second.json] [-c] [-r] [-h]"
    echo ""
	echo "  -a      Additional feature files"
    echo "  -c      Clean the application workspace"
    echo "  -r      Rebuild the Maven project"
    echo "  -u      Show usage information"
    echo ""
}

while getopts rcha: option
do
  case "${option}" in
    r) rebuild="1"
      ;;
    c) clean="1"
      ;;
	a)
	   for additional_feature_file in $(echo ${OPTARG} | tr ',' ' '); do
	     feature_files="${feature_files},src/main/features/${additional_feature_file}"
	   done
      ;;
    h)
      usage
      exit 1
      ;;
  esac
done

# rebuild
if [ ${rebuild} = "1" ] ;then 
    echo "--- BUILDING PROJECT ---"
    mvn -f .. clean install
fi

# resolve launcher
if [ ! -f ${launcher_jar} ]; then
    echo "--- DOWNLOADING LAUNCHER ---"
    mvn dependency:get -Dartifact=org.apache.sling:org.apache.sling.feature.launcher:${launcher_ver}
fi

# clean workspace
if [ ${clean} = "1" ]; then
    echo "--- CLEANING WORKSPACE ---"
    rm -rf launcher target/*
    mkdir -p target/storage
fi

# run
echo "--- LAUNCHING APPLICATION ---"
CUSTODE_HOME=target \
WORK_DIR=target \
HTTP_PORT=8082 \
ALL_FEATURES=${feature_files} \
LOGBACK_FILE=logback.xml \
LAUNCHER_JAR=${launcher_jar} \
 src/main/resources/files/usr/bin/custode
