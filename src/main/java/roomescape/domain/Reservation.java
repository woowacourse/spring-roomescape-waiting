package roomescape.domain;

import java.time.LocalDateTime;

public class Reservation {

    private final Long id;
    private final Reserver reserver;
    private final ReservationSlot slot;

    public Reservation(Long id, Reserver reserver, ReservationSlot slot) {
        validateReserver(reserver);
        validateSlot(slot);

        this.id = id;
        this.reserver = reserver;
        this.slot = slot;
    }

    public Reservation withId(Long id) {
        return new Reservation(id, reserver, slot);
    }

    public boolean isOwnedBy(Reserver reserver) {
        return this.reserver.equals(reserver);
    }

    public boolean isPast(LocalDateTime now) {
        return slot.isPast(now);
    }

    public boolean hasSameSchedule(Reservation other) {
        return slot.hasSameSchedule(other.getSlot());
    }

    public Long getId() {
        return id;
    }

    public Reserver getReserver() {
        return reserver;
    }

    public String getName() {
        return reserver.getName();
    }

    public ReservationSlot getSlot() {
        return slot;
    }

    private void validateReserver(Reserver reserver) {
        if (reserver == null) {
            throw new IllegalArgumentException("reserver는 비어 있을 수 없습니다.");
        }
    }

    private void validateSlot(ReservationSlot slot) {
        if (slot == null) {
            throw new IllegalArgumentException("slot은 비어 있을 수 없습니다.");
        }
    }
}
