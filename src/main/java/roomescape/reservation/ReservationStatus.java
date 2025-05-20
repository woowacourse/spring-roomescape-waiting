package roomescape.reservation;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ReservationStatus {
    PENDING("예약"),
    WAITING("예약대기");

    private final String message;
}
