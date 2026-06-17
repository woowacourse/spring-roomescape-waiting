package roomescape.dto.response;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ReservationStatus {
    RESERVED("예약"),
    PENDING_PAYMENT("결제대기"),
    WAITING("예약대기");

    private final String value;

    ReservationStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
