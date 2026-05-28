package roomescape.reservation.domain;

public enum Status {
    RESERVED,
    WAITING,
    ;

    public static Status from(boolean hasConfirmedReservation) {
        if (hasConfirmedReservation) {
            return WAITING;
        }
        return RESERVED;
    }
}
