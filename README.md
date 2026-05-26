## 실행 환경 설정

JWT 서명에 사용할 비밀 키는 소스에 하드코딩하지 않고 외부에서 주입합니다.

### 빠른 시작

프로젝트 루트에 `.env` 파일을 만들고 다음 내용을 채웁니다.

```properties
JWT_SECRET_KEY=<openssl rand -base64 32로 생성한 값>
JWT_EXPIRE_LENGTH=3600000
```

이후 평소처럼 실행합니다.

```bash
./gradlew bootRun
```

### JWT 키 생성

```bash
openssl rand -base64 32
```

### 환경 변수 명세

| 변수 | 필수 | 기본값 | 설명 |
| --- | --- | --- | --- |
| `JWT_SECRET_KEY` | 필수 | — | HS256 서명에 사용할 Base64 인코딩 키 (32바이트 이상 권장) |
| `JWT_EXPIRE_LENGTH` | 선택 | `3600000` | 토큰 만료 시간 (ms) |

`.env`는 `.gitignore`에 포함되어 커밋되지 않습니다.
값이 누락되면 앱 시작이 실패(fail-fast)합니다.

운영 환경에서는 `.env` 대신 OS 환경 변수 또는 시크릿 매니저(AWS Secrets Manager,
HashiCorp Vault 등)를 사용합니다. `spring.config.import`의 `optional:` 접두사
덕분에 `.env` 파일 없이 OS 환경 변수만으로도 동작합니다.

### 테스트 실행

```bash
./gradlew test
```

테스트는 별도 profile(`application-test.properties`)의 고정 키를 사용하므로
`.env` 설정 없이 실행 가능합니다.

#### 실행 및 확인 방법

- 애플리케이션 실행

```bash
./gradlew bootRun
```

- 사용자 예약 화면 접속

```text
http://localhost:8080
```

- H2 Console 접속

```text
http://localhost:8080/h2-console
```

- H2 접속 정보

```text
JDBC URL: jdbc:h2:mem:database
User Name: sa
Password: 비워두기
```

- 화면 확인용 초기 데이터는 `src/main/resources/data.sql`에 정의되어 있다.

## 기능명세서

### **1단계 - 예약 대기 신청/취소**

- [ ] 이미 다른 사용자에 의해 예약된 슬롯(날짜+시간+테마)에**대기를 신청**할 수 있다.
- [ ] 같은 슬롯에 대한 대기는**신청 순서대로 순번**이 부여된다.
- [ ] 같은 사용자가 같은 슬롯에**중복 대기할 수 없다**.
- [ ] 이미 지난 예약은 대기를 신청할 수 없다.
- [ ] 사용자는 본인의**대기를 취소**할 수 있다.
- [ ] [선택] 한 예약은 최대 20개의 예약 대기만 받을 수 있다.

### **2단계 - 내 예약 목록 조회 (상태 구분)**

- [ ] 이전 미션의 내 예약 목록 조회를**확장**한다.
- [ ] 사용자의**예약과 대기가 상태로 구분**되어 함께 표시된다.
- [ ] 대기에는 본인의**대기 순번**도 함께 보여준다.
