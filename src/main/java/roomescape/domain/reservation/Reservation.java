package roomescape.domain.reservation;

import roomescape.domain.DomainErrorCode;
import roomescape.domain.RoomEscapeException;

import java.time.LocalDateTime;

public class Reservation {
    private final Long id;
    private final ReservationName name;
    private final Status status;
    private final Slot slot;
    private final Rank rank;

    public Reservation(Long id, ReservationName name, Status status, Slot slot) {
        this(id, name, status, slot, null);
    }

    public Reservation(Long id, ReservationName name, Status status, Slot slot, Rank rank) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.slot = slot;
        this.rank = rank;
    }

    public static Reservation load(Long id, String name, String status, Slot slot) {
        return new Reservation(id, new ReservationName(name), Status.from(status), slot);
    }

    public static Reservation create(String name, Slot slot) {
        return new Reservation(null, new ReservationName(name), Status.WAITING, slot);
    }

    public Reservation withId(Long id) {
        return new Reservation(id, name, status, slot);
    }

    public Reservation withStatus(Status status) {
        return new Reservation(id, name, status, slot, rank);
    }

    public Reservation withRank(Rank rank) {
        return new Reservation(id, name, status, slot, rank);
    }

    public boolean isApproved() {
        return status == Status.APPROVED;
    }

    public boolean isWaiting() {
        return status == Status.WAITING;
    }

    public boolean isSameName(Reservation other) {
        return name.isSame(other.name);
    }

    public void validateCancellable(LocalDateTime now) {
        slot.validateNotPast(now);
    }

    public void validateOwner(String ownerName) {
        if (!name.isSame(new ReservationName(ownerName))) {
            throw new RoomEscapeException(DomainErrorCode.FORBIDDEN, ownerName);
        }
    }

    public Long getId() {
        return id;
    }

    public ReservationName getName() {
        return name;
    }

    public Status getStatus() {
        return status;
    }

    public Rank getRank() {
        return rank;
    }

    public Slot getSlot() {
        return slot;
    }

    public Long getSlotId() {
        return slot.getId();
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Reservation that)) return false;
        if (id == null || that.id == null) return false;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
