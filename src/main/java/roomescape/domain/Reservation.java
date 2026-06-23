package roomescape.domain;

import java.time.LocalDateTime;

public class Reservation {

    private final Long id;
    private final Reserver reserver;
    private final ReservationSlot slot;
    private final ReservationStatus status;

    public Reservation(Long id, Reserver reserver, ReservationSlot slot) {
        this(id, reserver, slot, ReservationStatus.CONFIRMED);
    }

    public Reservation(Long id, Reserver reserver, ReservationSlot slot, ReservationStatus status) {
        validateReserver(reserver);
        validateSlot(slot);
        validateStatus(status);

        this.id = id;
        this.reserver = reserver;
        this.slot = slot;
        this.status = status;
    }

    public Reservation withId(Long id) {
        return new Reservation(id, reserver, slot, status);
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

    public ReservationStatus getStatus() {
        return status;
    }

    public boolean isPending() {
        return status == ReservationStatus.PENDING;
    }

    public boolean isConfirmed() {
        return status == ReservationStatus.CONFIRMED;
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

    private void validateStatus(ReservationStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("status는 비어 있을 수 없습니다.");
        }
    }
}
