package roomescape.domain.waiting;

public class WaitingWithSequence {

    private static final int FIRST_WAITING_SEQUENCE = 1;

    private Waiting waiting;
    private Long sequence;

    protected WaitingWithSequence() {
    }

    public WaitingWithSequence(Waiting waiting, Long sequence) {
        this.waiting = waiting;
        this.sequence = sequence;
    }

    public boolean isPriority() {
        return sequence == FIRST_WAITING_SEQUENCE;
    }

    public Waiting getWaiting() {
        return waiting;
    }

    public Long getSequence() {
        return sequence;
    }
}
