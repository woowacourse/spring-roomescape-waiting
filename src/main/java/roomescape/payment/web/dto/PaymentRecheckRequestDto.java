package roomescape.payment.web.dto;

/**
 * 결과 불명확(NEEDS_CHECK) 주문의 재확인 요청. orderId로 주문을 찾아 저장된 paymentKey로 멱등 재confirm한다.
 */
public record PaymentRecheckRequestDto(String orderId) {
}
