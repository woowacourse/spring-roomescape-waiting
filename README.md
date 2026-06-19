# spring-roomescape-waiting

## 기능 요구 사항

- 예약 생성 흐름에 결제 단계를 끼워, 결제가 성공해야 예약이 CONFIRMED 되게 한다.
- 브라우저 Toss 결제창 SDK로 인증받고, 서버는 승인 API(POST /v1/payments/confirm)만 RestClient로 호출한다.
- 외부 에러 응답({code, message})을 도메인 예외로 변환하고, 금액 위변조를 승인 전에 차단한다.
- 외부 연동을 포트 & 어댑터(부패 방지 계층) 로 감싸 도메인이 Toss에 결합되지 않게 한다.

### 결제 연동

- 테스트 카드로 결제 인증 → 승인 → 예약 확정(CONFIRMED) 전체 흐름이 동작한다.
- 조작된 amount는 승인 호출 전에 차단되고 게이트웨이가 호출되지 않는다.
- 주요 에러코드(이미 처리됨/카드 거절/키 오류/재시도 대상)가 도메인 예외와 사용자 응답으로 처리되고, 미정의 코드는 기본 예외로 떨어진다.
- PaymentService가 Toss를 모른다(Toss DTO·에러 매핑이 어댑터 뒤로 격리됨).
- 시크릿 키가 설정으로 외부화되어 있고, failUrl의 orderId 없는 취소에서도 NPE가 없다.
