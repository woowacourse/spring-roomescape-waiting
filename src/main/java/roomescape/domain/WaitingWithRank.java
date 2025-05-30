package roomescape.domain;

public record WaitingWithRank(
        Waiting waiting,
        long rank
) {
}
