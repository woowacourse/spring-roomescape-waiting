package roomescape.payment.toss;

/**
 * 토스 승인 요청 바디. package-private — 어댑터 밖에선 참조할 수 없다(ACL의 컴파일러 강제).
 */
record TossConfirmRequest(String paymentKey, String orderId, long amount) {
}
