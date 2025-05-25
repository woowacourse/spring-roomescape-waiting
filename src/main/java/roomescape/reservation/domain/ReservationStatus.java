package roomescape.reservation.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReservationStatus {

    RESERVED,
    WAITING,
    CANCELED_RESERVATION,
    CANCELED_WAITING
    ;
}
