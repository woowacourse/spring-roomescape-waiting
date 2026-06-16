package roomescape.payment.infra.client.dto;

import lombok.Builder;

@Builder
public record CancelRequest(
        String cancelReason,
        Long cancelAmount,
        RefundReceiveAccount refundReceiveAccount,
        Long taxFreeAmount, // 취소할 금액 중 면세 금액
        String currency // 취소 통화
) {
}
