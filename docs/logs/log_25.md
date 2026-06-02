# 학습 로그 #25

**학습 범위**: WaitingService 조회 단순화 — "DB에 숨은 비즈니스 로직"을 도메인으로, DAO는 도메인 반환

## 1. 막힌 것의 종류

- [ ] 개념 자체를 모르겠다
- [ ] 개념은 알겠는데 코드로 어떻게 쓰는지 모르겠다
- [x] 코드는 돌아가는데 이게 맞는 건지 모르겠다
- [ ] 기타: ___

DAO 메서드명이 의미를 드러내지 못해서 헷갈렸다 (`findAllBySlotsOfMemberId` 같은). 더 본질적으로는 rank 계산이 SQL/Service 양쪽에 흩어져 있어 도메인이 빈 껍데기였다.

## 2. 이번 타임의 학습 내용

### 진단: API 응답 기준으로 rank의 필요/불필요를 가른다

- rank 필요: `GET /waitings`, `GET /manager/waitings`, `GET /admin/waitings`, `POST /waitings`
- rank 불필요: `DELETE /…/waitings/{id}`, 권한 검증, 존재 확인
- → 즉 "응답에 rank가 들어가는 경로"만 `Waitings`를 거치면 된다.

### 후보 탈락법

```
가설 A: 메서드명만 정리한다 (findAllByMemberId 등)
  ↓
findAllByMemberId 후 rank를 붙이려면 슬롯을 다시 조회해야 함
  → 쿼리 두 번, N+1 위험
  → 탈락
```

```
가설 B: DAO가 List<Waiting>을 반환, Service가 그룹핑/rank 계산
  ↓
"rank 계산"은 비즈니스 규칙인데 Service에 흩어진다
  → 도메인이 빈 껍데기
  → 탈락
```

```
가설 C: DAO가 List<Waitings>(도메인)를 반환, rank 계산은 Waitings가 책임
  ↓
- 쿼리 한 번
- "대기열 안의 rank"는 Waitings에 응집
  → 채택
```

### 트레이드오프를 도메인 특성으로 끊어내기

- 단점: 슬롯의 모든 Waiting을 통째로 가져옴 → 일반적으론 페이징/제한이 필요
- 도메인 특성: 방탈출은 취소가 거의 없음 → **대기 인원을 5명으로 제한**해서 단점 자체를 없앰
- 일반 해법(페이징, N+1 회피 트릭) 대신 도메인 지식으로 받아들임

### 결과 구조

```
DAO:      findQueueBySlot(slot) → Waitings
          findQueueBySlotForUpdate(slot)
          findAllQueues() → List<Waitings>
          findQueuesByStoreId(storeId)
          findQueuesContainingMember(memberId)

Waitings: assignRanks()
          assignRanksOfMember(memberId)

Service:  List<Waitings>를 flatMap으로 펼치기만 함
```

핵심: "rank가 필요한 조회는 대기열을 가져온다"가 타입으로 드러난다.

## 3. 전략 평가

**효과적이었던 것**

Codex가 메서드명 정리 방향으로 끌고 갈 때, user가 먼저 **"쿼리가 두 번 나가는 건 좋지 않다"** 로 끊어내고 구조 논의로 끌어올린 순간. 이게 오늘의 전환점이었다. 메서드명은 표면이고, 그 아래에 "DB에 숨은 비즈니스 로직"이 있다는 걸 스스로 짚었다.

**아쉬운 것**

Codex가 코드 적용까지 자동으로 진행했다. user가 의문을 던질 시간이 짧았다 — 변경된 4개 파일을 직접 읽고 "여기 왜 이렇게 했지?"를 묻는 단계가 없었다. 페어 프로그래밍 도구를 쓸 때 검증 루프를 어떻게 끼울지가 다음 과제다.

## 4. 이전 "바꿀 것" 적용 여부

- **log_24 바꿀 것** ("코치 힌트 없이 user가 먼저 패턴/구조 문제 꺼내기"): ✅ **적용됨**. Codex가 메서드명 안을 내밀었을 때 user가 먼저 "쿼리 두 번"이라는 구조 문제로 끌어올렸다. 코치가 가리키기 전에 user가 먼저 꺼낸 첫 번째 사례.
- **log_23 바꿀 것** ("패턴 이름 연결"): **룰 자체가 폐기됨** (5번 참조). 사용자 자기 진단으로 "패턴 이름이 본인 학습에 가치 있는지 잘 모르겠다 — 정보탐색용으로만 의미 있다"고 판단. 외부 룰이 아니라 본인 호기심에 맡기는 것으로 변경.

## 5. 다음 타임에 바꿀 것

**유지할 것**

코치가 안을 내밀었을 때 **"그 안의 전제"를 의심하는 것**. 오늘 Codex가 "메서드명 바꾸자"라고 한 순간, user는 그 안의 전제(`findAllByMemberId` 후 슬롯 재조회)를 의심했다. 표면을 받아들이지 않고 한 단계 위로 끌어올리는 흐름은 계속 가져간다.

**바꿀 것**

1. **AI 도구가 코드를 자동 적용했을 때 — 사용자가 의문 안 던지면 코치가 핵심 로직 한 가지를 추측 질문으로 강제로 보게 만든다.** "여기 왜 이렇게 했게요?" 같은 형태. 사용자 자율 신호 우선, 없을 때만 코치 트리거. **Why**: "변경사항 보지도 않고 OK하는 습관은 잘못된 것"이라는 본인 가치 판단. 평소 컨디션에서는 한 번쯤 멈추지만, 조급할 때(약속 등) 무너지므로 외부 강제 수단 필요.

**폐기된 항목**

~~"패턴 이름 연결" 룰~~ — 사용자가 "패턴 이름이 본인 학습에 가치 있는지 잘 모르겠다 — 정보탐색용으로만 의미 있다"고 진단. 코치가 외부 룰로 부과하던 것을 본인 호기심에 맡기는 형태로 이전. log_23 이래 누적 3~4회 미룸 카운트도 같이 폐기.

---

## 6. 추가 검토 사이클 (같은 날, /code-review xhigh 실행)

### 검토 결과

코드 리뷰가 14개 finding을 도출했고, 그 중 **#1, #3, #4, #7, #8, #13** 6개가 한 가지 원칙으로 묶였다: **"Waitings를 거치지 않으면 Waiting을 만들 수 없게 한다 = 모든 Waiting은 rank 부여된 상태로만 존재한다"**.

### 사용자가 그린 해법

`Waiting.create()`를 package-private으로 봉인 + `Waitings.enqueue(member, reservation)`이 검증·생성·rank 부여를 한 번에 처리 + `Waitings` 생성자가 받은 list에 rank 부여 후 보관. 즉 read path와 write path 양쪽에 같은 원칙이 적용된다.

### 적용 결과

- 도메인 변경: `Waiting.create` package-private, `Waiting.withId`/`withRank` 정리, `Waitings.enqueue` + 단순 게터(`getAll`, `ofMember`), `assign*` 메서드 전부 제거, `validateSameSlot` 오버로드를 이름 분리
- 인프라 변경: `WaitingJdbcDao.insert`에서 `findById` 재로드 제거, `WaitingService.create` 두 줄로 단순화
- **테스트 코드 변경 1줄** (`waitings.create` → `waitings.enqueue`). 5개 테스트 클래스 모두 그린.

### 학습 통찰

리뷰 finding이 단순한 결함 나열이 아니라, **한 가지 도메인 원칙(데이터를 가진 객체가 책임진다)이 여러 finding을 동시에 묶고 있다**는 게 보였다. 한 점을 풀면 여러 게 같이 풀린다 — 이게 "정렬된 설계"의 신호.

### 학습법 관점

- **5번 두 항목 모두 본인 자기 진단으로 형태 변경되거나 폐기됨.** 코치가 외부 룰로 부과하던 패턴이 한 사이클 안에서 정리됐다.
- **새 메모리 룰 2개 도출**: (1) "바꿀 것은 질문으로 끌어내기" (2) "AI 적용 후 강제 검토". 둘 다 "사용자 신호 우선, 없을 때만 코치 트리거"라는 같은 메커니즘.

## 다음 사이클 키워드

```
✅ composition vs association vs aggregation (log_23부터)
   → 맥락: Waitings가 List<Waiting>을 가지는 구조는 어느 분류인가
   → 종류: 개념 이해

✅ "DB에 숨은 비즈니스 로직" 찾는 안목
   → 맥락: 다른 DAO/Service에도 비슷한 게 있을까. ReservationDao/Service에서 같은 냄새가 나는 지점이 있는지 스캔
   → 종류: 실전 판단

✅ 동시성 race (리뷰 finding #6) 검토 + DB unique index 도입 여부
   → 맥락: MAX_WAITING_COUNT=5와 '중복 멤버' 불변식이 상위 reservation 락에만 의존. backstop으로 unique index를 둘지
   → 종류: 실전 판단
```
