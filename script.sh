#!/bin/sh
cd spring-roomescape-waiting
git pull origin step2
./gradlew bootJar -x test
nohup java -jar spring-roomescape-waiting-0.0.1-SNAPSHOT.jar >> app.log 2>&1 &
