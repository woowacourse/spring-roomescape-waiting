package roomescape.waiting.domain;

public record WaitingWithRank(Waiting waiting, Long rank) {

    public WaitingWithRank(Waiting waiting, int rank) {
        this(waiting, (long) rank);
    }
}
