# 더 생각해보기 — 타임아웃과 멱등 재시도

> 3차(타임아웃 방어 · 멱등 재시도 · 주문/결제 내역) 미션의 "더 생각해보기" 5개 항목을 각각 정리한다.
> 코드 근거: [`TossPaymentGateway`](../../src/main/java/roomescape/payment/gateway/toss/TossPaymentGateway.java),
> [`PaymentOrderService`](../../src/main/java/roomescape/service/PaymentOrderService.java),
> 학습 테스트 [`TimeoutLearningTest`](../../src/test/java/roomescape/learning/TimeoutLearningTest.java).

---

## 1. read timeout 값과 토스 응답 시간이 거의 같은 경계값에서는 성공과 실패가 어떻게 갈릴까?

경계값(`read timeout ≈ 응답 시간`) 근처에서는 결과가 **비결정적**으로 흔들린다. 같은 요청이라도 GC,
네트워크 지터, 토스 측 부하에 따라 응답이 timeout보다 수 ms 빠르면 성공, 수 ms 느리면 실패한다.
즉 경계값 부근에서는 **"성공/실패"가 아니라 "성공/확인 필요"의 비율**이 응답 시간 분포의 꼬리에 의해 결정된다.

- timeout을 응답 시간의 **중앙값(p50) 근처**에 두면 절반이 "확인 필요"로 떨어져 재시도·조회 비용이 폭증한다.
- timeout을 **p99~p99.9보다 약간 위**에 두면 정상 응답은 거의 다 성공으로 수렴하고, 진짜로 비정상인
  소수만 잘라낸다.

**활용**: timeout 값은 코드에 박지 않고([`application.properties`](../../src/main/resources/application.properties)로
외부화) 운영 지표(토스 응답 시간 분포)를 보고 정한다. "느린 호출을 일찍 포기해야 성공 TPS가 유지된다"는
[학습 테스트의 TPS 측정](../../src/test/java/roomescape/learning/TimeoutLearningTest.java)에서 본 그대로,
timeout은 *정상 응답을 자르지 않는 선에서 최대한 짧게*가 원칙이다.

---

## 2. 결제 승인 호출에서 connect와 read 중 어느 쪽을 더 짧게 두는 게 합리적일까?

**connect를 더 짧게** 두는 것이 합리적이다. 현재 설정도 `connect 3s < read 30s`다.

- **connect 단계**는 "TCP 연결 수립"만 한다. 정상이라면 수십 ms면 끝나므로, 오래 걸린다는 건 서버가
  죽었거나 네트워크가 끊긴 비정상 신호다. 길게 기다릴 가치가 없고, 게다가 connect 실패는 *아무 일도
  일어나지 않은* 안전한 실패라 빨리 포기하고 재시도하는 게 낫다.
- **read 단계**는 토스가 카드사 승인까지 처리하는 시간을 포함한다. 정상 처리가 원래 수 초 걸릴 수 있어
  connect보다 넉넉해야 하고, 여기서 너무 일찍 끊으면 *실제로는 승인된* 결제를 "확인 필요"로 만들어
  버린다(항목 3).

요약: connect = "연결조차 안 되면 빨리 포기", read = "승인 응답은 기다릴 가치가 있으니 더 길게".

---

## 3. read timeout 결제를 "확인 필요"로 두는 것과 "실패"로 단정하는 것 중 무엇이 나을까?

**"확인 필요"가 낫다.** read timeout은 "안 된 것"이 아니라 "됐는지 모르는 것"이다. 토스 쪽에서는 이미
승인이 끝났을 수 있어, "실패"로 단정하고 사용자를 재결제로 유도하면 **이중 결제**가 난다. 그래서
[`PaymentResultUnknownException`](../../src/main/java/roomescape/payment/PaymentResultUnknownException.java)을
별도로 두고, 주문 상태를 `UNKNOWN`으로 기록해 내역에서 "확인 필요"로 보여준다.

다만 **"확인 필요"가 영구 상태로 방치되면 그것도 나쁘다.** 성공/실패로 수렴시키려면:

1. **결제 조회 API**: 토스의 결제 조회로 해당 `orderId`/`paymentKey`의 실제 상태(DONE/미승인)를 확인해
   `CONFIRMED` 또는 진짜 `FAILED`로 확정한다. (조회는 GET이라 부수효과 없이 반복 가능)
2. **멱등 재시도**: 같은 `Idempotency-Key`로 confirm을 다시 호출한다. 이미 승인됐다면 토스가 첫 응답을
   그대로 돌려주므로 이중 승인 없이 `CONFIRMED`로 수렴한다(§16). 안 됐다면 이번에 승인된다.

이 수렴 장치(결제 조회/멱등 재시도)는 [TODO](../../CHANGES.md)로 남겨 두었다.

---

## 4. read timeout(됐는지 모름)과 connect 실패(연결조차 못 함)는 재시도 안전성이 다를까?

**다르다.**

| | connect 실패 | read timeout |
|---|---|---|
| 토스 도달 여부 | 닿지 못함 | 닿았을 수 있음 |
| 승인 가능성 | **없음**(확실) | **있음**(불명확) |
| 단순 재시도 | 안전 | 위험(멱등키 없으면 이중 승인) |
| 우리 처리 | `PENDING` 유지(재시도 안전) | `UNKNOWN`(확인 필요) |

connect 실패는 요청이 서버에 전달되지 않았음이 확실하므로 **그냥 다시 보내도 안전**하다. 그래서 상태를
`PENDING`으로 유지한다([`PaymentOrder.retryable`](../../src/main/java/roomescape/domain/PaymentOrder.java)).

read timeout은 *이미 승인됐을 수 있으므로* 아무 보호 없이 재시도하면 이중 승인이다. 그래서 재시도를 하되
반드시 **같은 멱등키로** 해야 안전하다. 멱등키가 connect/read 안전성 차이를 메워, 두 경우 모두 "그냥 같은
키로 다시 보내면 된다"로 단순화해 준다.

---

## 5. 진짜 connect 타임아웃은 블랙홀 IP로만 재현된다 — connect timeout이 없으면?

닫힌 포트는 OS가 즉시 `RST`로 거부(`ConnectException: Connection refused`)하므로 **connect timeout이
아니다.** 진짜 connect timeout은 `SYN`에 아무 응답이 없어 핸드셰이크가 멈추는 경우로, 사설/블랙홀
대역(예: `10.255.255.1:81`)으로만 재현된다.

이때 **connect timeout이 없으면** OS 기본 연결 타임아웃(플랫폼별 수십 초~수 분)까지 그 스레드가 묶인다.
요청이 몰리면 스레드 풀이 이 죽은 연결들로 가득 차 **결제와 무관한 요청까지 멈춘다.**

[학습 테스트 `connectTimeout()`](../../src/test/java/roomescape/learning/TimeoutLearningTest.java)에서
`10.255.255.1:81`로 이를 가볍게 실험했다: connect timeout(1s)을 걸면 약 1초 만에
`ResourceAccessException`(root `SocketTimeoutException` "Connect timed out")으로 끊기고, OS 기본값까지
가지 않음을 경과 시간 단언으로 확인할 수 있다. 이것이 connect timeout을 **반드시** 설정해야 하는 이유다.
