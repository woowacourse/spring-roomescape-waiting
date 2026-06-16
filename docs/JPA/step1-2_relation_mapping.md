# 1-2. JPA 연관관계 매핑

다른 클래스에 의존하는 클래스에 연관관계를 매핑합니다.
예: Reservation은 ReservationTime, Theme을 참조합니다.

## 요구사항

- `@ManyToOne + @JoinColumn(name = "..._id")`으로 객체 참조
- 단방향으로 시작합니다. 양방향이 필요한 이유가 생기면 그때 추가합니다.
- 양방향 시도 시 연관관계 주인 명시, 무한 직렬화 가능성 검토.
- `cascade`, `orphanRemoval`은 필요해질 때까지 적용하지 않습니다. 적용한다면 PR 본문에 그 근거를 적습니다.
- 양방향 또는 cascade를 한 번 시도했다가 단방향/제거로 후퇴하는 사이클을 의식적으로 한 번 굴려봅니다. 시도와 후퇴를 기록에 남기는 것이 차원 B(설계 판단)의 도달점입니다.

## 체크리스트

- [x] #1 `Reservation` JPA 엔티티 선언
  `@Entity`, `@Table(name="reservation")`, `@Id`, `@GeneratedValue(IDENTITY)` 추가. JPA 요구사항인 no-arg constructor(`protected`) 추가, `final` 필드 제거. `status`에 `@Enumerated(EnumType.STRING)` 적용.

- [x] #2 `Reservation` → `ReservationTime`, `Theme` 단방향 `@ManyToOne` 매핑
  `time` 필드에 `@ManyToOne @JoinColumn(name = "time_id")`, `theme` 필드에 `@ManyToOne @JoinColumn(name = "theme_id")` 추가. `cascade`, `orphanRemoval` 미적용.

- [x] #3 양방향 매핑 시도 후 단방향으로 후퇴 (설계 판단 기록)
  `ReservationTime` 또는 `Theme`에 `@OneToMany(mappedBy = ...)` 추가해 양방향을 의식적으로 시도한다. 무한 직렬화 가능성, 불필요한 컬렉션 노출 등을 검토한 뒤 단방향으로 후퇴하고 이유를 기록한다.

  **시도:** `ReservationTime`에 `@OneToMany(mappedBy = "time") List<Reservation> reservations` 추가.

  **검토 결과 및 단방향 후퇴 근거:**
  1. **불필요한 컬렉션**: `ReservationTime`은 도메인적으로 자신을 참조하는 예약 목록을 알 필요가 없다. 탐색 방향이 항상 `Reservation → ReservationTime`이지 역방향은 사용처가 없다.
  2. **무한 직렬화 가능성**: 엔티티를 직접 직렬화하거나 getter를 추가하는 순간 `ReservationTime → Reservation → ReservationTime → ...` 순환 참조로 `StackOverflowError` 발생. `@JsonIgnore`나 `@JsonManagedReference` 없이는 방어 불가.
  3. **연관관계 주인 혼란**: FK(`time_id`)는 `reservation` 테이블에 있으므로 실제 주인은 `Reservation.time`. `ReservationTime.reservations`를 통해 관계를 조작해도 JPA가 UPDATE를 무시해 의도치 않은 동작 발생 가능.
  4. **N+1 위험**: `@OneToMany`는 기본 LAZY지만 실수로 접근하면 Reservation 전체를 로딩하는 쿼리가 추가로 발생.

  **결론:** 양방향이 필요한 비즈니스 이유(예: `ReservationTime`에서 예약 목록을 직접 순회해야 하는 유스케이스)가 생길 때까지 단방향 유지.