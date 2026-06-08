package roomescape.common.event;

import roomescape.common.domain.ReservationSlot;

public class ReservationEvent {

    private final ReservationSlot slot;

    public ReservationEvent(ReservationSlot slot) {
        this.slot = slot;
    }

    public ReservationSlot getSlot() {
        return slot;
    }
}
