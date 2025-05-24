package roomescape.reservation.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ReservationStatus {
    PENDING("예약"),
    WAITING("대기"),

    ;

    private final String message;
}
