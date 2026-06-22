package roomescape.domain.payment;

import roomescape.domain.exception.InvalidDomainException;

/**
 * 결제 승인 요청에 필요한 최소 정보. 게이트웨이로 들어가는 입력 모델. 특정 PG사(Toss) 개념이 전혀 없다.
 */
public record PaymentConfirmation(String paymentKey, String orderId, long amount) {

    public PaymentConfirmation {
        if (paymentKey == null || paymentKey.isBlank()) {
            throw new InvalidDomainException("paymentKey는 비어 있을 수 없습니다.");
        }
        if (orderId == null || orderId.isBlank()) {
            throw new InvalidDomainException("orderId는 비어 있을 수 없습니다.");
        }
        if (amount <= 0) {
            throw new InvalidDomainException("결제 금액은 0보다 커야 합니다.");
        }
    }
}
