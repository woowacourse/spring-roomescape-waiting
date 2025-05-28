package roomescape.reservation.domain;

import lombok.Getter;

@Getter
public enum Status {
    BOOKED("예약"),
    WAITING("대기"),
    ;

    private final String value;

    Status(String value) {
        this.value = value;
    }
}
