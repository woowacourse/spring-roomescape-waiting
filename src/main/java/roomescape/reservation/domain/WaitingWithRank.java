package roomescape.reservation.domain;

import lombok.Getter;

@Getter
public class WaitingWithRank {

    private final Waiting waiting;
    private final Long rank;

    public WaitingWithRank(final Waiting waiting, final Long rank) {
        if (rank <= 0) {
            throw new IllegalArgumentException("예약 대기의 우선 순위는 1이상 이어야 합니다.");
        }

        this.waiting = waiting;
        this.rank = rank;
    }
}
