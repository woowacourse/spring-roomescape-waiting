package roomescape.reservation.domain;

import lombok.Builder;
import roomescape.global.exception.RoomEscapeException;

public record PaymentAmount(Long value) {

    @Builder
    public PaymentAmount {
        validateValue(value);
    }

    private static void validateValue(Long value) {
        if (value == null || value <= 0) {
            throw new RoomEscapeException("결제 금액은 양수여야 합니다.");
        }
    }
}
