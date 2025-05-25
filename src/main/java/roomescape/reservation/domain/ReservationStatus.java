package roomescape.reservation.domain;

public enum ReservationStatus {

    RESERVE("예약"),
    WAITING("예약대기");

    private String value;

    ReservationStatus(final String value) {
        this.value = value;
    }
}
