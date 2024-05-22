package roomescape.domain;

public enum ReservationStatus {
    RESERVED("예약"),
    WAITING("대기");

    private final String message;

    ReservationStatus(final String message) {
        this.message = message;
    }

    public String getMessageWithRank(final Integer rank) {
        if (this == WAITING) {
            return "%d번째 예약대기".formatted(rank);
        }
        return message;
    }
}
