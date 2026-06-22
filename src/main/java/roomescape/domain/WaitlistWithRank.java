package roomescape.domain;

public record WaitlistWithRank(
    Waitlist waitlist,
    long waitingOrder
) {

    public int waitingOrderAsInt() {
        return Math.toIntExact(waitingOrder);
    }
}
