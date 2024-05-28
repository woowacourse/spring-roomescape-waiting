package roomescape.domain.reservation;

public record WaitingWithRank(
        Waiting waiting,
        long rank
) {
}
