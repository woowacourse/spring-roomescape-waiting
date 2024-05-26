package roomescape.domain.waiting;

public class WaitingWithSequence {

    private final Waiting waiting;
    private final Long sequence;

    public WaitingWithSequence(Waiting waiting, Long sequence) {
        this.waiting = waiting;
        this.sequence = sequence;
    }

    public Waiting getWaiting() {
        return waiting;
    }

    public Long getSequence() {
        return sequence;
    }
}
