package roomescape.reservation.domain;

public record Rank(long count) {

    private static final long WAITING_INDEX = 1;

    public long getWaitingCount() {
        return count + WAITING_INDEX;
    }
}
