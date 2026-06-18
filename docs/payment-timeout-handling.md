# 결제 승인 타임아웃과 연결 실패 처리

## 목적

결제 승인 API 호출에서 발생하는 실패는 크게 두 종류로 나뉜다.

첫 번째는 Toss가 정상적으로 응답을 내려준 실패다. 이 경우 응답 본문에 `{code, message}`가 있고, 카드 거절이나 잘못된 요청처럼 Toss가 결제 요청을 명확히 거절한 상황이다.

두 번째는 Toss 응답을 받지 못한 실패다. 연결 자체가 실패했거나, 연결은 됐지만 응답을 읽기 전에 타임아웃이 난 경우다. 이 경우에는 Toss가 결제를 거절했다고 볼 수 없다. 특히 read timeout은 Toss 내부에서 승인이 완료됐지만 우리 서버가 성공 응답만 받지 못했을 가능성이 있다.

그래서 결제 흐름에서는 Toss 에러와 네트워크 실패를 분리해서 처리한다.

## 처리 위치

처리는 `TossPaymentGateway`에서 한다.

```java
try {
    response = restClient.post()
            .uri(CONFIRM_URI)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Idempotency-Key", confirmation.idempotencyKey())
            .body(new TossConfirmRequest(confirmation.paymentKey(), confirmation.orderId(),
                    confirmation.amount()))
            .retrieve()
            .onStatus(HttpStatusCode::isError, (request, clientResponse) -> {
                TossErrorResponse errorResponse = readErrorResponse(clientResponse.getBody().readAllBytes());
                throw TossPaymentException.of(clientResponse.getStatusCode(), errorResponse);
            })
            .body(TossConfirmResponse.class);
} catch (RestClientException e) {
    throw TossPaymentException.fromNetworkFailure(e);
}
```

`onStatus`는 Toss가 HTTP 에러 응답과 JSON 본문을 내려준 경우만 처리한다. 반면 타임아웃, 연결 거부, 네트워크 단절은 `RestClientException` 계열로 감싸져 나오기 때문에 `catch`에서 별도로 도메인 예외로 변환한다.

## 예외 분류

| 상황 | 표면 예외 | root cause | 도메인 예외 | HTTP 상태 | 의미 |
| --- | --- | --- | --- | --- | --- |
| 연결 거부 | `ResourceAccessException` | `ConnectException` | `ConnectionFailed` | 503 | Toss에 승인 요청을 보내지 못함 |
| 연결 타임아웃 | `ResourceAccessException` | `SocketTimeoutException` | `ConnectionFailed` | 503 | Toss에 연결하지 못함 |
| 응답 읽기 타임아웃 | `RestClientException` | `SocketTimeoutException` | `ConfirmationUnknown` | 504 | 승인 결과를 모름 |
| 기타 네트워크 오류 | `RestClientException` | 기타 원인 | `NetworkFailure` | 503 | 네트워크 계열 실패 |

## Toss 에러와의 차이

Toss 에러는 Toss가 요청을 처리한 뒤 명확한 결과를 응답한 것이다.

예를 들어 `REJECT_CARD_PAYMENT`는 카드사가 결제를 거절했다는 뜻이다. 이 경우 사용자에게 카드 거절 안내를 보여줄 수 있다.

반면 타임아웃은 Toss가 거절했다는 뜻이 아니다. 우리 서버가 응답을 받지 못했다는 뜻이다. 특히 read timeout은 승인 요청이 Toss에 도달했고, Toss 내부에서 결제가 승인됐을 수도 있다.

따라서 read timeout을 "결제 실패"로 단정하면 안 된다.

## 사용자 안내 기준

`ConnectionFailed`는 승인 요청이 Toss에 제대로 전달되지 못한 상황으로 본다.

사용자 메시지는 다음 방향이 적절하다.

```text
결제 승인 요청을 보낼 수 없습니다. 잠시 후 다시 시도해주세요.
```

`ConfirmationUnknown`은 승인 결과를 모르는 상황이다.

사용자 메시지는 다음 방향이 적절하다.

```text
결제 승인 응답을 받지 못했습니다. 결제가 완료됐는지 확인한 뒤 다시 시도해주세요.
```

이 메시지는 실패라고 단정하지 않는다. 사용자가 이미 카드 결제를 완료했을 가능성이 있기 때문이다.

## read timeout 처리 흐름

read timeout은 다음 상태를 의미할 수 있다.

```text
우리 서버 -> Toss: 승인 요청 전송 완료
Toss: 승인 처리 완료 가능성 있음
Toss -> 우리 서버: 성공 응답 전달 실패 또는 지연
우리 서버: 승인 성공 여부를 모름
```

따라서 read timeout이 발생하면 예약을 바로 확정해서도 안 되고, 바로 실패로 정리해서도 안 된다.

현재 처리 흐름은 다음과 같다.

```text
successUrl 콜백
-> amount 검증
-> Toss confirm 호출
-> read timeout 발생
-> TOSS_CONFIRMATION_UNKNOWN 예외
-> payment_order.payment_key 저장
-> 사용자에게 결제 승인 결과를 확인해야 한다는 메시지 응답
```

이 응답은 결제 실패 확정이 아니다. 승인 결과를 모르는 상태라는 의미다. 저장된 `paymentKey`가 있기 때문에 내 예약 화면에서는 이 주문을 `확인 필요`로 표시한다.

이후 운영 수준에서는 다음 후속 처리가 추가로 필요하다.

1. `paymentKey`로 Toss 결제 조회 API 호출
2. 조회 결과의 `orderId`, `amount`, 결제 상태를 우리 DB의 주문 정보와 비교
3. 승인 완료 상태면 예약을 `RESERVED`로 확정
4. 승인되지 않은 상태면 `PAYMENT_PENDING` 유지 또는 실패 처리
5. 조회도 실패하면 결과 불명확 상태를 저장하고 재확인 대상에 넣음

현재 구현은 read timeout을 `TOSS_CONFIRMATION_UNKNOWN`으로 분리한다. 아직 Toss 결제 조회 API를 통한 자동 복구는 구현되어 있지 않다.

## 테스트

`TossPaymentGatewayTest`에서 다음 케이스를 검증한다.

- `ConnectException`은 `TOSS_CONNECTION_FAILED`로 변환된다.
- `"Connect timed out"` 메시지를 가진 `SocketTimeoutException`은 `TOSS_CONNECTION_FAILED`로 변환된다.
- `"Read timed out"` 메시지를 가진 `SocketTimeoutException`은 `TOSS_CONFIRMATION_UNKNOWN`으로 변환된다.
- Toss가 `{code, message}`를 내려준 HTTP 에러는 기존 Toss 에러 매핑을 따른다.

이 구분 때문에 카드 거절 같은 명확한 Toss 거절과, 응답을 받지 못한 네트워크 실패가 사용자 응답과 후속 처리에서 섞이지 않는다.
