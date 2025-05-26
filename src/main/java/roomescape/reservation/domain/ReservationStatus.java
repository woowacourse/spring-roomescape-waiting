package roomescape.reservation.domain;

import lombok.Getter;

@Getter
public enum ReservationStatus {

    BOOKED("예약"),
    PAID("결제 완료"),
    ;

    private final String description;

    ReservationStatus(final String description) {
        this.description = description;
    }
}
