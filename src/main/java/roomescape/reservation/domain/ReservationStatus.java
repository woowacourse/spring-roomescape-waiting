package roomescape.reservation.domain;

public enum ReservationStatus {

    RESERVATION("예약"),
    WAITING("예약대기")
    ;

    private static final String RANK_SUFFIX = "번째 ";

    private final String status;

    ReservationStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public String formatRankWithSuffix(long rank) {
        return rank + RANK_SUFFIX + getStatus();
    }
}
