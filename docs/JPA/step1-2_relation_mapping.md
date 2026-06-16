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

- [ ] #3 양방향 매핑 시도 후 단방향으로 후퇴 (설계 판단 기록)
  `ReservationTime` 또는 `Theme`에 `@OneToMany(mappedBy = ...)` 추가해 양방향을 의식적으로 시도한다. 무한 직렬화 가능성, 불필요한 컬렉션 노출 등을 검토한 뒤 단방향으로 후퇴하고 이유를 기록한다.