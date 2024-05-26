## 어플리케이션 실행 방법
1. 도커를 실행한다.
```
docker-compose -p rooescape up -d
```
2. 어플리케이션을 실행한다.
3. 도커를 종료한다.
```
docker-compose -p rooescape down
```

## 기능 요구 사항
### 사용자 예약 조회하기
- Request
```
GET /reservations/mine HTTP/1.1
cookie: token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6IuyWtOuTnOuvvCIsInJvbGUiOiJBRE1JTiJ9.vcK93ONRQYPFCxT5KleSM6b7cl1FE-neSLKaFyslsZM
host: localhost:8080
```
- Respnse
```
HTTP/1.1 200 
Content-Type: application/json
[
    {
        "reservationId": 1,
        "theme": "테마1",
        "date": "2024-03-01",
        "time": "10:00",
        "status": "예약"
    },
    {
        "reservationId": 2,
        "theme": "테마1",
        "date": "2024-03-01",
        "time": "10:00",
        "status": "1번째 예약대기"
    },
    {
        "reservationId": 3,
        "theme": "테마3",
        "date": "2024-03-01",
        "time": "14:00",
        "status": "예약"
    }
]
```
### 에약 대기하기
#### 예약 대기 생성
- Request
```
POST /reservations HTTP/1.1
content-type: application/json
cookie: token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6IuyWtOuTnOuvvCIsInJvbGUiOiJBRE1JTiJ9.vcK93ONRQYPFCxT5KleSM6b7cl1FE-neSLKaFyslsZM
host: localhost:8080

{
    "date": "2030-4-18",
    "timeId": 1,
    "themeId": 1,
    "status": "WAITING"
}
```
- Response
```
HTTP/1.1 201 
Content-Type: application/json
{
    "id": 2,
    "memberName": "미아",
    "date": "2030-04-18",
    "time": {
        "id": 1,
        "startAt": "15:00"
    },
    "theme": {
        "id": 1,
        "name": "레벨2 탈출"
    }
}
```
#### 예약 대기 취소
- Request
```
DELETE /reservations/1/waiting HTTP/1.1
cookie: token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6IuyWtOuTnOuvvCIsInJvbGUiOiJBRE1JTiJ9.vcK93ONRQYPFCxT5KleSM6b7cl1FE-neSLKaFyslsZM
host: localhost:8080
```
- Response
```
HTTP/1.1 204
```
### 예약 대기 관리하기
#### 예약 대기 목록 조회
- Request
```
GET /admin/reservations/waiting HTTP/1.1
cookie: token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6IuyWtOuTnOuvvCIsInJvbGUiOiJBRE1JTiJ9.vcK93ONRQYPFCxT5KleSM6b7cl1FE-neSLKaFyslsZM
host: localhost:8080

[
    {
        "id": 2,
        "memberName": "미아",
        "date": "2030-05-19",
        "time": {
            "id": 1,
            "startAt": "15:00"
        },
        "theme": {
            "id": 1,
            "name": "레벨2 탈출"
        }
    }
]
```
#### 예약 대기 취소
- Request
```
DELETE /admin/reservations/waiting/2 HTTP/1.1
cookie: token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6IuyWtOuTnOuvvCIsInJvbGUiOiJBRE1JTiJ9.vcK93ONRQYPFCxT5KleSM6b7cl1FE-neSLKaFyslsZM
host: localhost:8080
```
- Response
```
HTTP/1.1 204
```
#### 예약 대기 승인
- 예약 취소시 자동으로 승인된다.
