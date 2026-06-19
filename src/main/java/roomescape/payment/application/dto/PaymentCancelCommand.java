package roomescape.payment.application.dto;

import lombok.Builder;
import roomescape.payment.presentation.dto.PaymentCancelRequest;

@Builder
public record PaymentCancelCommand(
        String name,
        Long cancelAmount,
        String cancelReason
) {
    public static PaymentCancelCommand toCommand(PaymentCancelRequest request) {
        return PaymentCancelCommand.builder()
                .name(request.name())
                .cancelAmount(request.cancelAmount())
                .cancelReason(request.cancelReason())
                .build();
    }
}
