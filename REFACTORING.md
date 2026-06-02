# 리팩토링 결과 보고

`origin/step1` 기준 변경 요약입니다. 리뷰 피드백 반영 + 추가 개선.

---

## 1. 도메인 풍부화 (Reservation Aggregate)

리뷰 코멘트: *"절차적 코드 나열로 보이는데 도메인 객체에 위임해보는 게 좋을 것 같네요"*

### AS-IS
```java
// Service 가 정책 조합을 직접 수행
ReservationTime time = findReservationTime(timeId);
Reservation reservation = findReservation(reservationId);
reservation.validateStoreOwnership(manager);
validatePastReservationCreate(date, time.getStartAt());   // Service 의 시간 산수
```

### TO-BE
```java
// 도메인 메서드가 정책 조합 + 변경 후 상태 반환
Reservation existing = findReservation(reservationId);
ReservationTime newTime = findReservationTime(timeId);
Reservation updated = existing.changeToByManager(manager, date, newTime);
return reservationDao.update(updated);
```

추가 도메인 메서드: `create`, `changeTo`, `changeToByManager`, `cancelBy`, `promoteTo`

---

## 2. DAO 시그니처 객체화

### AS-IS
```java
Long insertWithKeyHolder(Long memberId, LocalDate date, Long timeId, Long themeId, Long storeId);
int updateById(Long id, LocalDate date, Long timeId);
void updateMemberId(Long reservationId, Long memberId);
```

### TO-BE
```java
Reservation insert(Reservation reservation);
Reservation update(Reservation reservation);
// updateMemberId 는 update(Reservation) 로 통합
```

raw 필드 5개 행렬 시그니처 → 객체 단위 Repository 스타일.

---

## 3. Package-by-Feature

### AS-IS
```
roomescape/
├── controller/   (9개 모두)
├── service/      (7개 모두)
├── dao/          (6개 모두)
├── domain/       (6개 모두)
├── dto/{request,response}/
└── exception/{도메인}/
```

### TO-BE
```
roomescape/
├── auth/         (controller + service + dto + exception + 인프라)
├── reservation/  (controller + service + dao + domain + dto + exception)
├── reservationwait/
├── reservationtime/
├── theme/
├── member/
├── store/
└── common/       (BusinessException, GlobalExceptionHandler 등)
```

→ feature 변경 = 한 패키지만 건드림. `AdminStoreReservationController` → `ManagerReservationController` 리네임 포함.

---

## 4. 테스트 전략 보강

리뷰 코멘트: *"데이터의 변화가 어떻게 이루어졌는지 확인하는 것이 중요한 부분으로 보여서 mock은 사용하지 않는 것이 좋아 보입니다"*

### AS-IS
Service mock 단위 테스트만 존재. DB 동작 / 트랜잭션 / 제약 검증 불가.

### TO-BE
| 계층 | 도구 | 검증 |
| --- | --- | --- |
| 도메인 | POJO 단위 | 불변식, 도메인 정책 |
| DAO | `@JdbcTest + H2 + @Sql` | SQL · 매핑 · 제약 |
| **Service Mock** | Mockito | 호출 패턴, 예외 변환 (fence) |
| **Service Integration** (신규) | `@JdbcTest + @Import` | **트랜잭션, 제약, JDBC 예외 흐름** |
| Controller | `@SpringBootTest + RestAssured` | HTTP, 인증 |

`@Nested` 로 *Mock ↔ Integration* 1:1 대칭 구조. 모든 테스트에 *given / when / then* 주석 적용 (단순 검증은 제외).

---

## 5. 멱등 삭제

### AS-IS
`Theme`, `ReservationTime` 의 *존재하지 않는 ID 삭제* 시 404 반환.

### TO-BE
204 반환 (멱등). `ThemeNotFoundException` 제거.

---

## 6. 인덱스 / 쿼리 최적화 (성능)

리뷰 코멘트: *"인덱스를 고려하여 성능적인 이슈에 대응해볼 수 있게 고민해보시면 좋을 것 같아요"*

로컬 MySQL 실험 후 의사결정. 상세: [`docs/index-experiment-plan.md`](./docs/index-experiment-plan.md)

### `findEarliestMemberId` (자동 양도)
- **AS-IS**: `ORDER BY created_at` 인덱스 미지원 → `Using filesort`
- **TO-BE**: `(reservation_id, created_at)` 복합 인덱스 추가 → Sort 제거

### `findWaitingsByMemberId` (내 대기 목록)
- **AS-IS**: 윈도우 함수가 *전체 테이블 스캔 + 전체 정렬 + 후필터*
  ```sql
  SELECT ... FROM (
      SELECT ROW_NUMBER() OVER (PARTITION BY reservation_id ORDER BY created_at)
      FROM reservation_wait
  ) AS ranked WHERE ranked.member_id = ?
  ```
- **TO-BE**: correlated subquery 로 *member 먼저 필터 → 각 행의 순번 계산*
  ```sql
  SELECT ...,
      (SELECT COUNT(*) + 1 FROM reservation_wait sub
       WHERE sub.reservation_id = rw.reservation_id
         AND sub.created_at < rw.created_at) AS order_num
  FROM reservation_wait rw
  WHERE rw.member_id = ?
  ```
- `(member_id)` 인덱스 추가 (H2 호환 위해 명시)

### 실험 핵심 결과
- 매칭 행 수 < 100 → 인덱스 효과 미미 (0.91~0.97x)
- 매칭 행 수 ≥ 1000 → **19x** 빠름 (Sort 제거)
- INSERT 추가 비용 ≈ 0
- 본 도메인은 *현재 무용 영역* 이지만 추가 비용 0 이라 *선제 적용*
