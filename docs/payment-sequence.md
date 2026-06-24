# 토스 결제 연동 시퀀스 흐름도

방탈출 예약 결제(토스페이먼츠 결제창 연동)의 전체 흐름. Stage 0a 산출물.

- 실선 = 요청·호출, 점선 = 응답/리다이렉트
- `clientKey`(공개, 프론트) / `secretKey`(비밀, 서버) 는 둘 다 토스가 발급, 서버 config에 보관
- **인증(결제창) ≠ 승인(서버 confirm)**: 카드사는 두 단계 모두에서 토스 뒤에 있다

```mermaid
sequenceDiagram
    actor 사용자
    participant 클라이언트
    participant 서버
    participant DB
    participant 토스
    participant 카드사

    Note over 서버: 서버 config (토스에서 직접 발급)<br/>· clientKey(공개키) · secretKey(비밀키)
    클라이언트->>사용자: 예약 페이지 표시 (SSR · clientKey 주입)
    사용자->>클라이언트: 테마·날짜·시간 선택 후 '예약 결제하기'
    클라이언트->>서버: 예약 생성 요청 (테마 · 날짜 · 시간 · 예약자명)
    Note over 서버: ① 주문 생성 (서버 내부)<br/>· 예약 검증 후 PENDING 예약 생성<br/>· orderId(UUID) · customerKey 발급<br/>· 금액 50,000원 확정 (서버 권위)
    서버->>DB: 예약 · 주문 저장
    Note over 서버,DB: DB 저장 내용<br/>· 예약: 예약자·날짜·시간·테마, status=PENDING<br/>· 주문: orderId(UUID), 50,000원, orderName
    서버-->>클라이언트: orderId · amount(50,000원) · orderName · customerKey
    Note over 클라이언트: SDK 초기화<br/>· clientKey = 서버가 SSR로 주입 (공개키)<br/>· customerKey = 서버에서 받은 고객 식별자
    클라이언트->>토스: payment.requestPayment()<br/>amount · orderId · orderName · successUrl · failUrl
    사용자->>토스: 결제창에서 카드 정보 입력 · 인증
    토스->>카드사: 카드 인증 요청
    카드사-->>토스: 인증 완료
    alt 인증 성공
        토스-->>클라이언트: successUrl 리다이렉트<br/>paymentKey · orderId · amount
        클라이언트->>서버: POST /payments/{orderId}/confirmation<br/>paymentKey · amount
        Note over 서버: ② 승인 전 검증 (서버 내부)<br/>· orderId로 저장된 주문 조회<br/>· 요청 금액 = 저장 금액 검증 (위변조 차단)
        서버->>토스: POST /v1/payments/confirm (secretKey)<br/>paymentKey · orderId · amount
        토스->>카드사: 카드 승인(매입) 요청
        카드사-->>토스: 승인 / 거절
        토스-->>서버: 승인 결과 (완료 / 실패)
        Note over 서버: ③ 결제 결과 처리 (서버 내부)<br/>· 성공: 예약 CONFIRMED · 결제 저장<br/>· 실패·타임아웃: PENDING 정리 · 예외
        서버->>DB: 예약 CONFIRMED · 결제 저장
        서버-->>클라이언트: 예약 완료 응답
    else 결제창에서 실패·취소
        토스-->>클라이언트: failUrl 리다이렉트<br/>code · message · orderId
        클라이언트->>서버: DELETE /payments/{orderId}
        Note over 서버: 실패 정리 (서버 내부)<br/>· PENDING 예약·주문 삭제
        서버-->>클라이언트: 실패 안내 페이지
    end
```
