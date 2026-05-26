# 실행 가이드

이 프로젝트는 Spring Boot를 기반으로 한 방탈출 예약 관리 시스템입니다.

## 문서 목록

| 번호 | 문서 |
|---:|---|
| 1 | [API 명세서](docs/API명세서.md) |
| 2 | [ERD (Entity Relationship Diagram)](docs/ERD.md) |

## 사전 요구 사항
- Java 17 이상
- Gradle 8.x 이상 (프로젝트에 포함된 Gradle Wrapper 사용 권장)

## 실행 방법

### 1. 프로젝트 빌드
터미널에서 프로젝트 루트 디렉토리로 이동한 후 아래 명령어를 실행합니다.
```bash
./gradlew build
```

### 2. 애플리케이션 실행
아래 명령어를 통해 애플리케이션을 실행할 수 있습니다.
```bash
./gradlew bootRun
```

실행이 완료되면 `http://localhost:8080`에서 접속 가능합니다.

## 주요 접속 정보
- **애플리케이션 홈**: [http://localhost:8080](http://localhost:8080)
- **H2 콘솔 (인메모리 DB)**: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
    - **JDBC URL**: `jdbc:h2:mem:database`
    - **User Name**: `sa`
    - **Password**: (공란)

## 기능 테스트
프로젝트에 포함된 테스트 코드를 실행하여 기능이 정상적으로 동작하는지 확인할 수 있습니다.
```bash
./gradlew test
```
