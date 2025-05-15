# 방탈출 예약 대기

## 개요

방탈출과 예약 내역을 관리하고, 사용자가 방탈출을 예약할 수 있도록 돕는 웹 애플리케이션입니다.

## 제공 기능

방탈출과 예약을 관리하는 `관리자`와 방탈출을 예약하는 `사용자`의 기능을 제공합니다.

[Quick Start](#quick-start) 섹션으로 이동하여 애플리케이션을 실행하고 기능을 체험할 수 있습니다.

## 요구사항 (변경 사항)

- JPA 도입
    - [x] JPA 의존성 추가
    - [x] Entity 매핑
    - [x] 연관관계 매핑
    - [x] JPA Repository 도입
- 내 예약 목록 조회 기능
    - [x] 예약 목록 화면 추가
    - [x] 예약 목록 확인 API 구현
        - 테마, 날짜, 시간, 상태를 응답한다.

# Quick Start

애플리케이션 실행 후 아래의 링크로 접속할 수 있습니다.

- 관리자 페이지: [localhost:8080/admin](http://localhost:8080/admin)
- 사용자 페이지: [localhost:8080/](http://localhost:8080/)

## 더미 계정

관리자와 사용자의 더미 계정은 아래와 같습니다. 회원가입 후 로그인도 가능합니다.

```text
관리자 계정: admin@email.com
비밀번호: password
사용자 계정: normal@email.com
비밀번호: password
```
