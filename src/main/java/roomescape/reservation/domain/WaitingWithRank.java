package roomescape.reservation.domain;

public record WaitingWithRank(Waiting waiting, Long rank) {

    public WaitingWithRank {
        if (rank <= 0) {
            throw new IllegalArgumentException("예약 대기의 우선 순위는 1 이상 이어야 합니다.");
        }
    }
}
