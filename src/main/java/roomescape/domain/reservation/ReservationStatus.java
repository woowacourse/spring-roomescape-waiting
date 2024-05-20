package roomescape.domain.reservation;

public enum ReservationStatus {
    COMPLETE("예약"),
    WAITING("대기");
    private final String value;

    ReservationStatus(final String value) {
        this.value = value;
    }

    public static ReservationStatus from(final String value) {
        for (final ReservationStatus reservationStatus : ReservationStatus.values()) {
            if (reservationStatus.value.equals(value)) {
                return reservationStatus;
            }
        }
        throw new IllegalArgumentException(String.format("%s 는 역할에 없습니다.", value));
    }

    public String getValue() {
        return value;
    }
}
