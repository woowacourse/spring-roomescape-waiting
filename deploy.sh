#!/bin/bash

# 1. 현재 실행 중인 스프링 부트 프로세스 PID 찾기
CURRENT_PID=$(pgrep -f spring-roomescape-waiting)

# 2. 실행 중인 프로세스가 있으면 안전하게 종료
if [ -z "$CURRENT_PID" ]; then
    echo "> 현재 실행 중인 애플리케이션이 없으므로 종료하지 않습니다."
else
    echo "> 현재 실행 중인 애플리케이션(PID: $CURRENT_PID)을 종료합니다."
    kill -15 $CURRENT_PID
    sleep 5
fi

# 3. 최신 코드 가져오기 및 빌드
echo "> 최신 코드를 pull 받고 빌드를 시작합니다."
git pull origin step2

# gradlew 실행 권한 부여 후 빌드
chmod +x ./gradlew
./gradlew bootJar

# 4. 새 jar 파일 백그라운드 실행
echo "> 새 애플리케이션을 백그라운드로 배포합니다."
cd build/libs
nohup java -jar spring-roomescape-waiting-0.0.1-SNAPSHOT.jar > /dev/null 2>&1 &

echo "> 배포가 완료되었습니다!"
