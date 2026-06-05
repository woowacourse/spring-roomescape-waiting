package roomescape.slot.domain;

public class SlotOccupancy {

    private final boolean reserved;
    private final boolean waitingExists;

    private SlotOccupancy(boolean reserved, boolean waitingExists) {
        this.reserved = reserved;
        this.waitingExists = waitingExists;
    }

    public static SlotOccupancy of(boolean reserved, boolean waitingExists) {
        return new SlotOccupancy(reserved, waitingExists);
    }

    public boolean isReservable() {
        return !reserved && !waitingExists;
    }

    public boolean isWaitable() {
        return reserved || waitingExists;
    }

}
