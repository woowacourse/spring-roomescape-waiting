package roomescape.reservation;

public enum ReservationStatus {
    CONFIRMED("예약 확정"),
    PENDING_PAYMENT("결제 대기");

    private final String description;

    ReservationStatus(String description) {
        this.description = description;
    }

    public static ReservationStatus from(String raw) {
        if (raw == null) {
            return CONFIRMED;
        }
        return valueOf(raw);
    }

    public String getDescription() {
        return description;
    }
}
