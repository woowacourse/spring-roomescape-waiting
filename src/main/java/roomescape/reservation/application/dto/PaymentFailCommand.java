package roomescape.reservation.application.dto;

import java.time.LocalDateTime;

public record PaymentFailCommand(
        String orderId,
        LocalDateTime now
) {

    public boolean hasOrderId() {
        return orderId != null && !orderId.isBlank();
    }

    public String normalizedOrderId() {
        return orderId.trim();
    }
}
