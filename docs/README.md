##페어 프로그래밍
### 1단계 - 예약 대기 신청/취소
- [x] 이미 다른 사용자에 의해 예약된 슬롯(날짜+시간+테마)에 대기를 신청할 수 있다.
- [x] 같은 슬롯에 대한 대기는 신청 순서대로 순번이 부여된다.
- [x] 같은 사용자가 같은 슬롯에 중복 대기할 수 없다.
- [x] 사용자는 본인의 대기를 취소할 수 있다.

### 2단계 - 내 예약 목록 조회 (상태 구분)
- [x] 이전 미션의 내 예약 목록 조회를 확장한다.
- [x] 사용자의 예약과 대기가 상태로 구분되어 함께 표시된다.
- [x] 대기에는 본인의 대기 순번도 함께 보여준다.

### 기능 목록
- [x] 에약 대기 기능 추가
- [x] 예약 대기 순번 계산
- [x] 예약 대기 취소 기능 추가
- [x] 예약 및 대기 조회 기능 추가
- [x] 예약 가능한 시간에 대기 인원 정보 추가
- [x] 예약 수정 기능
- [x] 예약 취소 기능

### 예외 처리
- [x] 같은 슬롯에 대한 중복 대기 불가 예외처리
- [x] 본인의 예약이 아닌 경우 대기 취소 불가
- [x] 이미 날짜가 지난 예약 대기는 취소 불가 


# 리팩토링

- [x] Exception 정의 스타일 변경
- [x] schema.sql: status 유지 WAITING 추가 (order 제외 -> 비즈니스 로직 판단)
    - [x] displayStatus 따로 구현: 응답에는 status에 COMPLETED 추가
- [x] Controller의 ResponseEntity 일관되게 구현
- [x] 재정렬 로직이 각 쓰기 로직에 추가 (사이클 2에서 해야 하긴 한데..)
- [x] Service -> domain 책임 재분배 (domain 을 최대한 활용하기)
- [x] Service 동시성 제어
    - [x] exists() 검증 + try-catch로 race condition 수습 + DB unique 제약
- [x] 전체적으로 일관성있는 네이밍으로 변경
- [x] Service 통합 테스트 진행 - DirtyContext 삭제하기

# 리팩토링

- [x] 예외 정의 및 처리 스타일 정리
- [x] Controller 응답을 `ResponseEntity` 기반으로 일관화
- [x] 예약 상태 구조 정리
    - [x] `schema.sql`에 `WAITING` 상태 추가
    - [x] 대기 순번 컬럼 제거, 순번은 비즈니스 로직에서 계산
    - [x] 조회 응답용 `DisplayStatus` 추가, 완료된 예약은 `COMPLETED`로 표시
- [x] 예약 생성/수정/취소 시 대기 승격 및 순번 재계산 흐름 반영
- [x] Service 책임 일부를 Domain으로 이동
    - [x] 예약 생성, 수정, 취소 가능 여부 검증을 도메인 메서드로 위임
- [x] 예약 동시성 제어 보강
    - [x] Service 레벨 `exists()` 사전 검증
    - [x] DB unique 제약 추가
    - [x] `DuplicateKeyException` 처리로 race condition 보정
    - [ ] 예약 대기 동시성 제어(따닥) 아직 미흡
      - Reservation이 2개 들어왔을 경우
      - 한 사용자가 한 스케줄에 중복 저장 성공했을 경우
- [x] 전체 네이밍 일관성 정리
- [x] 테스트 구조 재정리
    - [x] DAO: `@JdbcTest`
    - [x] Service: Mockito 단위 테스트
    - [x] Controller: `@WebMvcTest`
    - [x] Domain 단위 테스트 추가
    - [x] `@DirtiesContext` 의존 제거
