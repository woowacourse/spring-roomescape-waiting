#!/bin/sh
set -e

cd /home/ubuntu/spring-roomescape-waiting

git pull
./gradlew bootJar
./gradlew --stop

cd build/libs
PID=$(pgrep -f spring-roomescape-waiting-0.0.1-SNAPSHOT.jar || true)

if [ -n "$PID" ]; then
    kill "$PID"
fi

nohup java -jar spring-roomescape-waiting-0.0.1-SNAPSHOT.jar >> app.log 2>&1 &