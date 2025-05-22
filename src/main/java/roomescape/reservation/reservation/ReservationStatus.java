package roomescape.reservation.reservation;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ReservationStatus {
    CONFIRMED("예약"),
    WAITING("예약대기");

    private final String message;
}
