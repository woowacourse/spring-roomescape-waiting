package roomescape.reservation;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ReservationStatus {
    PENDING("예약");

    private final String message;
}
