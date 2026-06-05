package roomescape.domain.reservation;

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

    public static Reservation create(String name, Status status, Slot slot) {
        return new Reservation(null, new ReservationName(name), status, slot);
    }

    public Reservation withId(Long id) {
        return new Reservation(id, name, status, slot);
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

    public boolean isSameName(String other) {
        return name.isSame(other);
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
