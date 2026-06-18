package roomescape.payment.web;

import roomescape.payment.ConfirmOutcome;

/**
 * confirm 응답. 성공/확인필요를 status로 구분해, 프론트가 '확정'과 '확인 필요'를 다르게 안내하게 한다.
 */
public record PaymentConfirmResponse(ConfirmOutcome status) {
}
