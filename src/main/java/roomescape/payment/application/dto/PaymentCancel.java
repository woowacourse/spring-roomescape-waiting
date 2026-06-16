package roomescape.payment.application.dto;

import lombok.Builder;
import roomescape.payment.infra.client.dto.RefundReceiveAccount;

@Builder
public record PaymentCancel(
        String paymentKey,
        String cancelReason,
        Long cancelAmount,
        RefundReceiveAccount refundReceiveAccount,
        Long taxFreeAmount, // 취소할 금액 중 면세 금액
        String currency // 취소 통화
) {
}
