# 🚀 4단계: 계층 분리
## 요구사항
ReservationController에 웹 요청 처리·비즈니스 로직·DB 접근이 모두 몰려 응집도는 낮고 결합도는 높은 상황이다. 레이어드 아키텍처로 레이어별 책임에 따라 코드를 분리한다.

⚠️ 새로운 테스트 도구나 기법을 도입하지 않고 레벨1에서 학습했던 JUnit만 활용한 단위 테스트에 집중한다. 요구사항에서 RestAssured가 주어진 경우 그대로 사용하되, 그 위에 새 테스트 기법을 쌓지 않는다.

레이어별 책임과 역할에 따라 클래스를 분리하고, 분리한 클래스를 Spring Bean으로 등록한다.

| 레이어 | 책임 |
| :--- | :--- |
| Controller | 웹 요청·응답 |
| Service | 비즈니스 플로우 |
| DAO (Repository) | DB 접근 |
| Domain | 비즈니스 규칙 |

- ReservationController에 JdbcTemplate 필드가 남아있지 않아야 한다

## (선택) 콘솔 UI 지원
현재 예약·시간 관리 기능은 웹에서만 쓸 수 있는 상황이다. 같은 기능을 콘솔에서도 쓸 수 있게 만들어, 계층 분리가 실제로 "다른 UI에 재사용 가능한 구조"를 만들었는지 검증한다. 계층 분리의 효과를 검증하고 싶은 크루만 진행한다.

- 시간 · 예약 관리 기능을 콘솔에서도 사용할 수 있도록 콘솔 UI 추가
- 콘솔 UI가 만드는 데이터는 메모리에 저장 (DB와 분리)

## 요구사항 테스트
아래 테스트가 통과하면 단계 4 완료.

```java
@Autowired
private ReservationController reservationController;

@Test
void 계층화_리팩터링() {
boolean isJdbcTemplateInjected = false;

    for (Field field : reservationController.getClass().getDeclaredFields()) {
        if (field.getType().equals(JdbcTemplate.class)) {
            isJdbcTemplateInjected = true;
            break;
        }
    }

    assertThat(isJdbcTemplateInjected).isFalse();
}
```

## 힌트
막힐 때 참고. 처음부터 모두 읽을 필요는 없다.

### 어디서 시작할지 모르겠다면
- 컨트롤러는 웹 요청/응답 책임만 가지도록 수정한다.
- DB 접근 책임은 DAO(Data Access Object)에 위임한다.
- 비즈니스 플로우 책임은 서비스에 위임한다.
- 비즈니스 규칙 책임은 도메인에 위임한다.

### Spring Bean 등록
분리한 클래스를 빈으로 등록할 때 @Component · @Service · @Repository 등을 활용할 수 있다. 매번 새로운 인스턴스를 만들지 않고 스프링 컨테이너가 관리하도록 설정한다.

### (선택) 콘솔 UI
UI 로직과 비즈니스 로직이 명확하게 분리되어야 새로운 UI가 추가되더라도 기존 기능에 크게 영향을 받지 않는다. UI(웹, 콘솔)에 의존적이어서 재사용이 어려운 코드가 있는지 확인하고 리팩터링한다.
재사용이 필요한 코드가 무엇인지, 왜 재사용해야 하는지에 대한 고민을 해본다.
데이터 저장 방법이 메모리와 데이터베이스로 상이하다. 저장에 대한 내용을 추상화해본다.

### 키워드
- Bean Container, Spring Bean
- Layered Architecture
