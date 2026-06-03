## 학습 로그 #7

**시간**: 06/03 CQRS 리팩터링 중 이어진 정리
**학습 범위**: 예약 대기 유스케이스에서 ApplicationService가 CommandService와 QueryService를 조합하는 구조

### 1. 막힌 것의 종류

이번에 막힌 것은 어떤 종류의 어려움이었는가? (해당하는 것에 체크)

- [ ] 개념 자체를 모르겠다
- [ ] 개념은 알겠는데 코드로 어떻게 쓰는지 모르겠다
- [x] 코드는 돌아가는데 이게 맞는 건지 모르겠다 (CommandService와 QueryService를 나눈 뒤 흐름을 어디에 둘지)
- [ ] 기타: ___

### 2. 이번 타임의 학습 전략

- 이전에 바꾸기로 한 전략은 무엇이었고, 실행했는가?

```

```

- 실제로 어떻게 학습했는지 디테일한 과정을 써보세요.

```
# 실제 학습 과정
- CQRS를 학습하면서 쓰기 작업과 읽기 작업을 나누기 위해 CommandService와 QueryService를 분리했다.
- 그런데 서비스를 나누고 나니 예약 대기 신청이라는 하나의 흐름이 여러 서비스에 흩어질 수 있었다.
- 예약 대기 신청은 단순히 ReservationWaiting을 저장하는 작업이 아니었다.
  1. 요청으로 들어온 timeId, themeId를 조회한다.
  2. date, time, theme로 Slot을 만든다.
  3. 해당 Slot에 실제 예약이 존재하는지 확인한다.
  4. 예약된 슬롯이 아니면 대기를 신청할 수 없다는 정책을 적용한다.
  5. 예약이 존재하면 ReservationWaiting을 저장한다.
  6. 저장 후 응답에 필요한 대기 순서를 포함한 읽기 모델을 다시 조회한다.
- 이 흐름을 CommandService 안에 넣으면 CommandService가 조회와 정책 조합까지 알게 된다.
- 반대로 QueryService 안에 넣으면 QueryService가 실패 정책과 저장 흐름까지 알게 된다.
- Controller에서 CommandService와 QueryService를 직접 조합하면 controller가 유스케이스를 알게 된다.
- 그래서 ReservationWaitingApplicationService를 두고,
  하나의 사용자 요청 흐름을 application layer에서 조합하도록 정리했다.
- 하위 QueryService는 조회 자체에 집중하도록 두었다.
  예를 들어 findBySlot은 Optional을 반환하고, 존재하지 않을 때 어떤 예외를 던질지는 application service에서 결정하도록 했다.
- 예약된 슬롯이 없을 때 "예약된 슬롯에만 대기를 신청할 수 있습니다."라는 정책 예외를 던지는 것도 application service에 두었다.
- 저장 자체는 ReservationWaitingCommandService가 맡고,
  저장 후 응답에 필요한 ReservationWaitingWithOrder 조회는 ReservationWaitingQueryService가 맡게 했다.
- ApplicationService는 이 둘을 조합해서 "예약 대기 신청"이라는 유스케이스를 완성한다.

# 후기
- 처음에는 CommandService와 QueryService를 나누면 구조가 충분히 정리될 것이라고 생각했다.
- 하지만 실제 사용자 흐름은 command와 query 중 하나로만 설명되지 않았다.
- 예약 대기 신청은 조회, 검증, 저장, 재조회가 모두 이어지는 유스케이스였다.
- 따라서 command/query 분리는 하위 작업의 성격을 나누는 데 도움이 되지만,
  사용자 요청 하나의 흐름을 표현하려면 그 위에 application service가 필요했다.
- 이번에 application service는 단순히 service를 한 번 더 감싼 계층이 아니라,
  유스케이스의 순서와 정책을 모으는 계층이라는 것을 더 구체적으로 이해했다.
```

### 3. 전략 평가

- 효과적이었던 것과 그 이유

```
# 효과적이었던 것
- CommandService와 QueryService의 분리 자체를 목표로 보지 않고, 유스케이스 흐름을 기준으로 다시 본 것
- 조회 실패를 어디에서 예외로 바꿀지 고민한 것
- 하위 QueryService는 순수 조회에 가깝게 두고, 정책 판단은 ApplicationService에 둔 것
- Controller가 여러 서비스를 직접 조합하지 않도록 한 것

# 이유
- 서비스를 command/query로 나누는 것만으로는 사용자 요청의 전체 흐름이 드러나지 않는다.
- 예약 대기 신청처럼 여러 조회와 저장이 섞인 흐름에서는 오케스트레이션 위치가 중요하다.
- ApplicationService에 흐름을 모으니 controller는 요청과 응답에 집중하고,
  하위 service는 각자 좁은 책임에 집중할 수 있었다.
```

- 비효과적이었던 것과 그 이유

```
# 비효과적이었던 것
- 처음에는 CommandService와 QueryService 중 어디에 흐름을 넣을지 고민한 것
- findBySlot 같은 조회 메서드에서 바로 정책 예외를 던지는 방식도 생각한 것

# 이유
- 예약된 슬롯이 없을 때 대기를 신청할 수 없다는 것은 단순 조회 규칙이 아니라 예약 대기 신청 유스케이스의 정책이다.
- QueryService가 이 정책까지 알게 되면 조회가 다른 맥락에서 재사용되기 어려워진다.
- CommandService에 조회 흐름까지 넣으면 쓰기 작업의 책임이 넓어진다.
- 결국 둘 중 하나에 억지로 넣기보다, 상위 application service에서 조합하는 것이 더 자연스러웠다.
```

- 막힌 것의 종류(1번)와 전략의 궁합은 어땠는가?

```
# 막힌 것의 종류와 전략 궁합
- 이번 막힘은 "코드는 돌아가는데 이 구조가 맞는지 모르겠다"는 유형이었다.
- 이런 문제는 클래스 이름이나 계층 이름보다 실제 유스케이스 흐름을 먼저 써보는 것이 도움이 되었다.
- 예약 대기 신청 흐름을 순서대로 적고 나니,
  어떤 작업은 조회이고 어떤 작업은 저장이며 어떤 판단은 유스케이스 정책인지 분리할 수 있었다.
```

### 4. AI 피드백

- 자신의 학습 전략에 대해 AI 학습 전문가에게 피드백을 요청하고, 유용했던 제안 1가지 이상 기록

```
# 나의 전략 분석
- CQRS를 적용하면서 command/query라는 이름에 집중하다 보니,
  실제 사용자 흐름을 어느 계층에서 책임질지 다시 고민하게 되었다.
- 이번에는 흐름을 먼저 적고, 각 단계의 책임을 나누는 방식으로 정리했다.

# AI 학습 전문가의 피드백
- CommandService와 QueryService는 작업의 성격을 나누는 도구일 뿐, 유스케이스를 자동으로 표현해주지는 않는다.
- 하나의 사용자 요청이 여러 조회와 저장을 조합한다면,
  그 순서와 정책은 application/use-case layer에 두는 것이 자연스럽다.
- 하위 서비스는 가능한 좁은 책임을 유지하고,
  "조회 결과가 없을 때 이 유스케이스에서는 어떤 의미인가" 같은 판단은 application service에서 처리하는 것이 좋다.

# 유용했던 제안
- 유스케이스를 구현할 때 먼저 아래처럼 나누어 본다.
  1. 단순 조회인가?
  2. 단순 저장인가?
  3. 여러 조회와 저장을 조합하는 사용자 흐름인가?
  4. 조회 결과를 정책 실패로 해석해야 하는가?
- 3번이나 4번에 해당하면 application service에서 흐름을 조합하는 것이 적절하다.

# 적용 결과
- ReservationWaitingApplicationService가 예약 대기 신청 흐름을 담당하게 했다.
- ReservationWaitingCommandService는 저장과 삭제 같은 변경 작업에 집중하게 했다.
- ReservationWaitingQueryService와 ReservationQueryService는 조회 자체에 집중하게 했다.
- 예약된 슬롯이 없다는 조회 결과를 ConflictException으로 바꾸는 정책은 application service에서 처리했다.
```

### 5. 다음 타임에 바꿀 것

- 유지할 것과 그 이유

```
# 유지할 것
- 유스케이스 흐름을 먼저 적고 계층 책임을 나누는 방식
- Controller가 여러 하위 서비스를 직접 조합하지 않도록 하는 방식
- 하위 QueryService는 조회 자체에 집중시키고, 정책 해석은 ApplicationService에서 처리하는 방식
- CommandService는 도메인 변경과 저장에 집중시키는 방식

# 이유
- 이 방식은 계층 이름보다 실제 책임을 기준으로 구조를 나눌 수 있게 해준다.
- 조회와 저장이 섞인 흐름에서도 어디에 정책을 둘지 판단하기 쉬워졌다.
- 특히 "조회 결과 없음"이 모든 상황에서 같은 의미가 아니라는 점을 인지하게 되었다.
```

- 바꿀 것과 그 이유

```
# 바꿀 것
- 새로운 기능을 구현할 때 controller, command service, query service부터 만들기보다 유스케이스 순서를 먼저 적는다.
- 메서드 이름도 repository 조회 모양보다 유스케이스에서 어떤 의미를 가지는지 기준으로 정한다.
- command/query 분리를 적용할 때도, 그 위에 흐름을 조합할 계층이 필요한지 먼저 확인한다.

# 이유
- 계층을 먼저 나누면 실제 흐름이 여러 곳으로 흩어질 수 있다.
- 반대로 유스케이스를 먼저 쓰면 각 계층이 맡아야 할 책임이 더 명확해진다.
- 앞으로는 "이 클래스가 command인가 query인가"보다
  "이 코드는 사용자 흐름인가, 단순 작업인가, 정책 해석인가"를 먼저 보아야겠다.
```
