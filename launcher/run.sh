#!/bin/sh -e

launcher_ver=0.2.0
launcher_jar=${HOME}/.m2/repository/org/apache/sling/org.apache.sling.feature.launcher/${launcher_ver}/org.apache.sling.feature.launcher-${launcher_ver}.jar

# build
echo "--- BUILDING PROJECT ---"
mvn -f .. clean install

# resolve launcher
if [ ! -f ${launcher_jar} ]; then
    echo "--- DOWNLOADING LAUNCHER ---"
    mvn dependency:get -Dartifact=org.apache.sling:org.apache.sling.feature.launcher:${launcher_ver}
fi

# run
echo "--- LAUNCHING APPLICATION ---"
rm -rf launcher target/*  && java -Dlogback.configurationFile=logback.xml -jar ${launcher_jar} -f app.json,config-w541.json
