package roomescape.payment.web.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 결제 준비 요청. 결제를 시작할 예약(PENDING)의 식별자만 받는다 — 금액은 서버가 테마 가격으로 정한다.
 */
public record PaymentReadyRequestDto(@NotNull Long reservationId) {
}
