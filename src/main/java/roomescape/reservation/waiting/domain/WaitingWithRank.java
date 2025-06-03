package roomescape.reservation.waiting.domain;

import roomescape.reservation.domain.ReservationStatus;

public class WaitingWithRank {

    private final Waiting waiting;
    private final long rank;

    public WaitingWithRank(final Waiting waiting, final long rank) {
        validate(waiting, rank);
        this.waiting = waiting;
        this.rank = rank;
    }

    private void validate(final Waiting waiting, final long rank) {
        if (waiting == null) {
            throw new IllegalArgumentException("예약 대기를 입력해야 합니다.");
        }
        if (rank < 0) {
            throw new IllegalArgumentException("우선 순위가 유요하지 않습니다.");
        }
    }

    public Waiting getWaiting() {
        return waiting;
    }

    public long getRank() {
        return rank;
    }

    public String getDescription(ReservationStatus status) {
        return (rank + 1) + "번째" + " " + status.getDescription();
    }
}
