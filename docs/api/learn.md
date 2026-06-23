결제 - 예약 - 확인 구조로 확장.

- 외부 API 연동과 예외 핸들링
- Connection Timeout 과 Read Timeout
- TPS 와 Rate Limit

`외부 API 를 어떻게 읽고 호출하고 검증하는가`

- 결제 흐름 이해
- 결제 승인 API 직접 호출

### 결제 흐름

![](https://static.tosspayments.com/docs/learn/payment-flow-new2.png)

- #### 1 결제 요청
    - 구매자 : 상품 정보/결제 금액 확인 후 결제하기 클릭
    - 클라이언트 : 클릭 이벤트로 토스페이먼츠 SDK 결제 요청 메서드 호출. 해당 결제창 열기.
      파라미터로 주문번호, 성공/실패 URL _정의_ 가능.
- #### 2 구매자 정보 인증
    - 구매자 : 카드정보 입력 / 앱카드 / 간편결제 앱으로 결제 정보 호출
    - 카드사 : 구매자 결제 정보 확인, 카드 소유자 인증
- #### 3 인증 결과 확인
    - 구매자 : 결제 흐름이 완료된 것으로 보임.
    - 개발자 : 정보 인증 성공 시 토스페이먼츠 결제 기록 생성, 결제 키 발급.  
      키는 각 결제에 고유한 값, 인증 종료 시 자동 발급.  
      이후 성공 URL 로 리다이렉트. 백/프론트 엔드포인트/페이지 무관. 결제 키/주문번호 등 결제 정보 전달.
- #### 4 결제 승인
    - 서버 : 리다이렉트된 성공 URL 쿼리파라미터 값이 요청 값과 동일한지 확인,  
      동일하면 결제 승인 API 호출, 인증된 결제 카드사로 결제 승인 요청 전달.
    - 카드사 : 결제 금액을 구매자 계좌/카드 에서 차감, 최종 결과는 API 응답으로 확인.

## 요청과 승인의 분리

- 데이터 정합성과 연동 편의를 위해.
- 결제 인증과 승인이 비동기적으로 발생하기에 결과 수신을 위해선 웹훅 연동 필요.
- 웹훅 연동 시 중간 중단이나 트래픽 유실 등 상황에서 불일치 데이터 존재 가능.
- 이에 요청과 승인을 분리함으로서 `데이터 정합성 보장, 상점 역할 감경`

### paymentKey 필요성

각 결제를 식별하는 값.

- 인증의 결과로 발급된 paymentKey.
- 성공 URL 쿼리 파라미터로 내려받아
    - 결제 승인
    - 결제 취소
    - 결제 조회
- 에 필요한 값.
- 이에 승인 이후 받은 Payment 객체에서 키를 꺼내 DB에 저장 필요.
- 시크릿 키 없이는 키값으로 아무 API 호출할 수 없기에 노출 무관.

## 결제 정보 검증

![](https://static.tosspayments.com/docs/learn/payment-data-check.png)

요청과 승인 과정 사이에서 결제 정보 검증을 구현하는 흐름.

### 결제 요청 전 - 결제할 데이터 저장

구매자 결제 정보는 결제 요청 전에 저장 필수. 요청 전후의 무결성 검증 목적.  
결제 금액에 적립금/쿠폰 등 변동이 존재한다면 최종 결제 금액을 서버에 저장 필요.  
실제 적립금과 쿠폰 등의 변동 요인의 존재 여부가 필요하기 때문.

`실행 흐름`

- 구매자가 주문서에서 결제하기 버튼 클릭해 결제 요청.
- 주문번호/최종 결제 금액 서버 세션이나 DB에 임시로 저장.
- 결제 정보가 저장된 것이 확인된 경우 실제 결제 요청 전달.

### 결제 승인 전 - 승인할 데이터 검증

결제 요청/인증 성공 시 최종 승인 요청 전에 정보 검증 필요.  
앞서 요청에서 저장한 정보와 인증 결과 정보의 동일성/무결성 검증하는 과정.

`실행 흐름`

- 결제 인증 완료 시 성공 URL 파라미터 값 확인. `paymentKey`, `orderId`, `amount` 상존
- `orderId` 로 결제 요청 전에 저장해 둔 임시 정보 로드
- 적립금/쿠폰 사용 가능 여부, 사용시 최종 결제 금액이 성공 URL 통한 `amount` 와 동일 확인
- 모두 문제 없으면 성공 URL 데이터로 결제 승인 요청.

## 정리

> 결제 요청 - 사용자/결제 인증 - 결제 생성 - 결제 검증 - 결제 승인

- 클라이언트 : 결제 화면/주문서 렌더링
- 클라이언트 : 결제 버튼 클릭 시 결제 요청
- 서버 : 사용자의 카드/결제 정보 인증, 카드사에 소유권 검증 요청
- 서버 : 인증 결과를 바탕으로 결제 정보 생성, 성공/실패 URL 리다이렉트
- 서버 : 리다이렉트 URL 분기, 파라미터 정보로 결제 내용 검증
- 서버 : 결제 무결성 확인, 결제 승인 요청, `결제`/`주문` 성립

---

## 1단계 · 결제 프로세스 이해하기 (이해)

> 목표: 토스 결제 연동 문서를 읽고 "결제 인증"과 "결제 승인"이 무엇인지,  
> 클라이언트와 서버가 각각 무슨 일을 하는지 그릴 수 있게 된다.

<p>먼저 <a href="https://docs.tosspayments.com/guides/v2/payment-widget/integration">결제 위젯 연동하기</a>와 <a href="https://docs.tosspayments.com/guides/v2/get-started/payment-flow">결제 흐름(Payment Flow)</a> 문서를</p>   
<p>"누가(클라이언트/서버) 무엇을 하는지"에 초점을 두고 가볍게 읽어보자.</p>

### 결제 위젯 연동 4단계

1. 렌더링 — 클라이언트가 renderPaymentWidget으로 결제 UI를 띄운다.
1. 요청 — 사용자가 결제 버튼을 누르면 클라이언트가 requestPayment로 요청한다.
1. 인증(결제 인증) — 사용자가 카드 정보를 입력/인증하고 카드사가 카드 소유권을 검증한다(사기 결제 방지).
1. 승인(결제 승인) — 서버가 승인 API를 호출해 결제 금액과 주문 정보를 검증한 뒤 실제로 돈이 빠져나간다. 결제가 최종 완료되는 단계다.

<table><thead><tr><th>구분</th><th>클라이언트(프론트엔드)</th><th>서버(우리가 구현할 부분)</th></tr></thead><tbody><tr><td>키</td><td><code>clientKey</code>(공개 키) 사용</td><td><code>secretKey</code>(비밀 키) 사용 — 절대 외부 노출 금지</td></tr><tr><td>하는 일</td><td>위젯 렌더링, 결제 요청, <code>successUrl</code>/<code>failUrl</code> 리다이렉트 처리</td><td>① 결제 전 <code>orderId</code>·최종 <code>amount</code> 미리 저장 ② 리다이렉트로 돌아온 값 검증 ③ 승인 API 호출</td></tr></tbody></table>

결제 정보 인증 완료 시 성공 URL 로 리다이렉트, 파라미터로 paymentKey, orderId, amount 가 전달.

- paymentKey 는 토스가 발급하는 거래 고유 식별자. 이후 CRUD 에 필요한 식별자이므로 DB 에 저장.
- 서버는 orderId, amount 가 사전 저장값과 일치하는지 확인, 무결 시 승인 API 호출.

`위 흐름에서 서버가 직접 코드로 호출하는 것은 4단계, 결제 승인 API 하나뿐?`

<blockquote>
<p>확인 포인트</p>
<ul>
<li>결제 인증을 끝까지 진행해 <code>successUrl</code>로 돌아왔는가?</li>
<li><code>paymentKey</code>, <code>orderId</code>, <code>amount</code> 세 값을 모두 확보했는가?</li>
<li>API 콘솔에서 승인 요청을 보내 <code>status: DONE</code> 응답을 받아봤는가?</li>
<li>같은 결제를 두 번 승인하면 어떤 <code>code</code>/<code>message</code>가 오는지 확인했는가?</li>
<li>콘솔 인증에 사용한 값이 <strong>테스트 시크릿 키</strong>(서버↔토스 사이의 인증 수단)임을 이해했는가?</li>
</ul>
</blockquote>

### 확인 포인트

- 결제 위젯 연동 4단계 중 서버가 직접 호출하는 단계(결제 승인)는 어느 것인가?

> 4단계, 결제 승인 API 호출

- successUrl로 어떤 값들이 넘어오고, 그중 서버가 검증해야 하는 값은 무엇인가?

> paymentKey, orderId, amount 가 전달된다. 키값으로 저장된 orderId, amount 각각을 검증

- clientKey와 secretKey는 각각 어디에 쓰이는지 구분할 수 있는가?

> 클라이언트키는 공개 키로서 식별자 역할, 단독으론 API 호출 불가  
> 시크릿키는 비밀 키로서 클라이언트키와 함께 API 호출에 활용

## 2단계 : 사이트에서 결제하고 승인 호출해보기(조작)

<blockquote>
<p>목표: 코드를 작성하지 않고 직접 손으로 두 가지를 해본다. ① 샌드박스에서 테스트 결제를 해 <code>paymentKey</code>/<code>orderId</code>/<code>amount</code>를 얻고, ② 그 값으로 결제 승인 API를 <strong>API 콘솔에서 직접 호출</strong>한다. 이렇게 "결제 인증 → 승인" 한 사이클을 끝까지 경험하는 것이 목적이다.</p>
</blockquote>

### 할 일 ①: 샌드박스에서 결제 인증해 값 얻기

<p>토스는 실제 출금이 없는 <strong>샌드박스(Sandbox)</strong> 환경과 테스트 키(<code>test</code>로 시작, 샌드박스에서만 동작)를 제공한다.</p>

샌드박스 테스트 결과

```json
POST: /v1/payments/confirm
{
"mId": "tgen_docs",
"lastTransactionKey": "txrd_a01kvq7r77d41rcrkfek6sd4537",
"paymentKey":"tgen_202606221738135BSI0",
"orderId": "MC45NzAwMzcxOTc2MTc1",
"orderName": "토스 티셔츠 외 2건","taxExemptionAmount": 0,
"status": "DONE",
"requestedAt":"2026-06-22T17:38:13+09:00",
"approvedAt": "2026-06-22T17:39:31+09:00",
"useEscrow": false,"cultureExpense": false,
"card":{
"issuerCode": "W1",
"acquirerCode": "W1",
"number":"53176460****129*",
"installmentPlanMonths": 0,
"isInterestFree": false,"interestPayer": null,
"approveNo": "00000000",
"useCardPoint":false,
"cardType": "신용",
"ownerType": "개인","acquireStatus": "READY",
"amount": 50000
},
"virtualAccount": null,
"transfer": null,
"mobilePhone": null,
"giftCertificate": null,
"cashReceipt": null,
"cashReceipts": null,
"discount": null,
"cancels": null,
"secret": "ps_DpexMgkW36bmqvWn0dl93GbR5ozO",
"type": "NORMAL",
"easyPay":{
"provider": "토스페이",
"amount": 0,
"discountAmount":0
},
"country": "KR",
"failure": null,
"isPartialCancelable": true,
"receipt":{"url": "https://dashboard-sandbox.tosspayments.com/receipt/redirection?transactionId=tgen_202606221738135BSI0&ref=PX"},
"checkout":{"url": "https://api.tosspayments.com/v1/payments/tgen_202606221738135BSI0/checkout"},
"currency": "KRW",
"totalAmount": 50000,
"balanceAmount": 50000,
"suppliedAmount": 45455,
"vat": 4545,
"taxFreeAmount": 0,
"method": "간편결제",
"version": "2022-11-16",
"metadata": null
}
```

<h3 id="user-content-할-일--api-콘솔에서-승인-요청-보내기"><a tabindex="-1" href="#할-일--api-콘솔에서-승인-요청-보내기"></a>할 일 ②: API 콘솔에서 승인 요청
보내기</h3>

결제 승인 테스트 결과

```json
요쳥
curl --request POST \
--url https: //api.tosspayments.com/v1/payments/confirm \
--header 'Authorization: Basic dGVzdF9za196WExrS0V5cE5BcldtbzUwblgzbG1lYXhZRzVSOg==' \
--header 'Content-Type: application/json' \
--data '{"amount":"50000","orderId":"MC45NzAwMzcxOTc2MTc1","paymentKey":"tgen_202606221738135BSI0"}'

400 응답
{
"code": "INVALID_API_KEY",
"message": "잘못된 시크릿키 연동 정보 입니다."
}
```

샌드박스에 표시된 시크릿 키 / orderId / paymentKey 로 재시도 - successUrl 응답 확인 