## 어떤 개념일까?

### 결론

외부 API 호출 구현 방식

- 새 프로젝트라면 단순 호출은 `RestClient`,
- 여러 엔드포인트를 인터페이스로 묶는다면 HTTP Interface(`@HttpExchange`) + RestClient adapter,
- reactive 스택이면 `WebClient`

`RestTemplate`은 2025-11에 deprecation 의도로 공지가 되었고 신규 코드에서는 쓰지 않는다고 한다.

테스트

- 클라이언트 구현에 독립적이고 진짜 HTTP를 검증하고 싶다면 `WireMock` / `MockWebServer`
- 빠른 슬라이스 단위로 충분하다면 @RestClientTest + MockRestServiceServer 로 한다고 한다.

### 외부 API 호출

내 애플리케이션이 다른 HTTP 서버에 클라이언트로서 요청을 보내고 응답을 객체로 역직렬화 해서 받는 것이다.

Spring에서 이걸 위한 클라이언트 추상화를 제공하고 있고 4가지의 선택지가 가능하다.

| 클라이언트 | 등장 | 모델 | 스타일 | 현재 위상 |
| --- | --- | --- | --- | --- |
| `RestTemplate` | Spring 3 | 동기/블로킹 | template (메서드 호출형) | 7.0에서 deprecate 의도 공지, 7.1에서 formal @Deprecate, 8.0에서 제거 예정 Spring |
| `WebClient` | Spring 5 | 비동기/논블로킹 | fluent + reactive (Mono/Flux) | reactive 스택의 표준 |
| `RestClient` | Spring 6.1 | 동기/블로킹 | fluent builder | **비-reactive 앱의 권장 선택** |
| HTTP Interface (`@HttpExchange`) | Spring 6 | 위 셋을 adapter로 사용 | 선언적(interface) | Feign 대체 |

---

## 어떤 문제를 해결하려고 나왔을까? 왜 사용 할까?

Spring 공식 블로그

각 클라이언트는 하나씩 해결하기 위해서 등장하게 되었다.

### RestTemplate의 한계

template 스타일의 API이라서 메서드 오버로드가 폭발하고 template 방식 API의 구조적 한계 때문에 RestClient와의 격차를 더 이상 좁힐 수 없는 수준에 이르게 되었다.

template 패턴이란?

### WebClient

블로킹 모델은 요청마다 스레드를 점유한다.
그래서 느린 외부 서비스를 기다리는 요청이 많아지게 되면 스레드/메모리가 고갈되게 된다.
이를 WebClient는 논블로킹으로 문제를 해결하였다.

다만 Mono/Flux/operator 같은 reactive 프로그래밍 패턴에 대한 이해가 필요하고, 애플리케이션에 다른 reactive 컴포넌트가 없다면 개념적인 오버헤드만 추가된다.

### RestClient

WebClient의 fluent함은 좋은데 reactive까지는 필요없다고 해서 등장하였다.

RestTemplate의 기존 HTTP 인프라를 재사용하고 같은 패키지에 위치시켜 마이그레이션을 쉽게 했고,

imperative, 블로킹 스타일은 유지하여서 별도 변경 없이 비동기 조합이 가능하다.

즉 Virtual Thread와의 궁합이 좋다고 한다.

### HTTP Interface

호출 코드의 보일러 플레이트를 제거하고 인터페이스에 어노테이션만 선언하면 구현체를 프록시로 만들어 준다.

---

## 어떻게 동작하나?

### RestClient 동기

```java
RestClient restClient = RestClient.builder()
        .baseUrl("https://api.example.com")
        .build();

User user = restClient.get()
        .uri("/users/{id}", id)
        .retrieve()
        .body(User.class);
```

### HTTP Interface 선언적

```java
interface UserApiClient {
    @GetExchange("/users/{id}")
    User getUser(@PathVariable Long id);
}
```

프록시로 빈 등록

```java
RestClient restClient = RestClient.builder().baseUrl("https://api.example.com").build();
RestClientAdapter adapter = RestClientAdapter.create(restClient);
HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
UserApiClient client = factory.createClient(UserApiClient.class);
```

HttpExchangeAdapter가 HTTP Interface 인프라를
실제 호출 클라이언트(RestClient, WebClient, RestTemplate)와 분리해주는 계약이다.

즉, 선언적 인터페이스는 그대로 두고 하부 엔진만 교체할 수 있도록 한다.

RestClient Spring 공식 문서

**동기와 비동기**

- `RestTemplate / RestClient`: 내부적으로 thread-per-request 모델 위에서 동작하기 때문에 응답을 받을 때까지 스레드가 블로킹 된다.
- `WebClient`: 이벤트 루프 기반의 논블로킹이고, 대기 중인 요청마다 전용 스레드 스택이 필요하지 않다.

---

## 언제 써야 할까?

### RestTemplate

- Spring MVC 서블릿 기반 신규 프로젝트의 기본값이고, 외부 호출 몇 개 하는 일반적인 백엔드일 때,
- 완전한 reactive까지 필요 없고 RestTemplate의 간단한 대체가 필요할때

### HTTP Interface @HttpExchange

한 외부 서비스에 엔드포인트가 여러 개여서 인터페이스로 응집하고 싶고, SDK처럼 다루고 싶을 때

### WebClient

애플리케이션이 이미 WebFlux/reactive 스택이거나, 스트리밍, 고동시성, 여러 외부 API 병렬 호출일 때

## 외부 API는 테스트 코드는 어떻게 작성해야할까?

중요한 점은 무엇을 격리해야할까에 따라서 도구가 달라진다고 생각한다.

외부 API 호출은 Service에 대한 테스트이다.

### MockRestServiceServer

직접 또는 간접적으로 사용하는 테스트의 진입점으로,
실제 서버 없이 예상 요청을 설정하고 mock 요청을 돌려준다.
동작 원리는 MockClientHttpRequestFactory로 HTTP 호출을 가로채서,
설정된 기대 요청 목록과 대응 응답을 만들어주고, 클라이언트가 호출하면
목록에서 찾아 응답을 반환하는 방식이다.

Spring Boot에서는 @RestClientTest로 슬라이스 테스트를 한다.
필요한 컴포넌트만 자동으로 구성하고 전체를 제외하여 테스트가 빠르게 가능하고,
MockRestServiceServer를 자동으로 구성해준다.

```java
@RestClientTest(UserService.class)
class UserServiceHttpTest {
    @Autowired UserService userService;
    @Autowired MockRestServiceServer mockServer;

    @Test
    void getUser_success() {
        mockServer.expect(requestTo("/users/1"))
                  .andExpect(method(HttpMethod.GET))
                  .andExpect(header("Authorization", "Bearer token123"))
                  .andRespond(withSuccess("""
                      {"id":1,"name":"Test User"}
                      """, MediaType.APPLICATION_JSON));

        User result = userService.getUser(1L);

        assertThat(result.name()).isEqualTo("Test User");
        mockServer.verify();   // 기대한 요청이 실제로 일어났는지 검증
    }

    @Test
    void getUser_throws_on_404() {
        mockServer.expect(requestTo("/users/999"))
                  .andRespond(withStatus(HttpStatus.NOT_FOUND));
        assertThatThrownBy(() -> userService.getUser(999L))
            .isInstanceOf(UserNotFoundException.class);
    }
}
```

주의점

- 상태 격리: MockRestServiceServer는 모든 테스트가 공유하는 자원이므로 각 테스트 후 reset해야 한다.
- 여러 RestClient 인스턴스가 있으면 자동 구성이 안된다.
- WebClient는 동작하지 않는다.
  OkHttp, WireMock 같은 mock server가 이미 존재하기 때문에
  WebClient용 우사 지원은 제공하지 않는다고 한다.
  레퍼런스에서도 WebClient 테스트에는 mock server를 권장한다고 한다.

### WireMock / MockWebServer로 실제 HTTP 서버를 띄운다.

진짜 포트를 열어서 stub 서버를 띄우므로, RestClient든 WebClient든 동일하게 테스트되고
직렬화, 헤더, 타임아웃, 재시도까지 실제 네트워크 경로로 검증이된다.

통합 테스트에서는 TestContainers의 MockServer 모듈로 선언적으로 HTTP 클라이언트의 외부 REST 연동을 테스트 하는 방법도 있다.

Spring Framework에서 논의 이슈

### 외부 API 상황별 테스트 방식 정리

| 무엇을 검증? | 도구 |
| --- | --- |
| 클라이언트 코드 로직(URL/헤더/역직렬화/에러 변환)을 빠르게 | `@RestClientTest` + `MockRestServiceServer` |
| 클라이언트 구현 무관 + 실제 HTTP + WebClient 포함 | WireMock / OkHttp `MockWebServer` |
| 컨테이너 기반 통합 테스트 | Testcontainers `MockServer` |

---

## 남에게 설명한다면 어떻게 설명할 것인가?

Spring에서는 외부의 API를 호출하는 도구들이 시간에 따라 다르게 되어 있다.
옛날엔 RestTemplate 블로킹 메서드로 사용했지만 지금은 API가 오래되고 문제점들이 있어서 쓰지 말라고 선언했다.

---

## 추가 궁금한 질문들

1. **RestClient + Virtual Thread**: Boot에서 `spring.threads.virtual.enabled=true`일 때 RestClient 블로킹 호출이 실제로 어떻게 동작하나? Structured Concurrency 조합은 어디까지 가능한가?
2. **타임아웃/커넥션 풀**: RestClient는 `ClientHttpRequestFactory`(Apache HttpClient/Jetty)에서 타임아웃을 설정해야 한다는데, 커넥션 풀·재시도·Resilience4j circuit breaker와는 어디서 합치는가?
3. **HTTP Interface 등록 자동화**: Spring 7 계열에서 `@ImportHttpServices` 류로 프록시 빈 등록을 어디까지 자동화할 수 있나? (직접 `HttpServiceProxyFactory` 짜는 보일러플레이트를 줄이는 방향)
4. **에러 모델 일관성**: 외부 API의 에러를 우리 도메인 예외로 변환할 때, 기존에 쓰던 `ProblemDetail` 기반 에러 핸들링과 어떻게 연결할 것인가?
5. **계약 테스트**: WireMock stub과 실제 외부 API 스펙이 어긋나는 문제(stub이 거짓말하는 경우)는 Spring Cloud Contract 같은 consumer-driven contract로 어디까지 막을 수 있나?