package roomescape.controller.dto.request;

import roomescape.domain.exception.DomainErrorCode;
import roomescape.domain.exception.RoomEscapeException;

public record ControllerPaymentConfirmRequest(
        String paymentKey,
        String orderId,
        Long amount
) {
    public ControllerPaymentConfirmRequest {
        if (paymentKey == null || paymentKey.isBlank()) {
            throw new RoomEscapeException(DomainErrorCode.INVALID_INPUT);
        }
        if (orderId == null || orderId.isBlank()) {
            throw new RoomEscapeException(DomainErrorCode.INVALID_INPUT);
        }
        if (amount == null) {
            throw new RoomEscapeException(DomainErrorCode.INVALID_INPUT);
        }
    }
}
