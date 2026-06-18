package roomescape.domain.reservation;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import roomescape.domain.DomainErrorCode;
import roomescape.domain.RoomEscapeException;

import java.time.LocalDateTime;

@Entity
@Table(
        uniqueConstraints = @UniqueConstraint(name = "uq_reservation",
                columnNames = {"slot_id", "name"}
        ))
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private ReservationName name;

    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToOne(optional = false)
    @JoinColumn(name = "slot_id")
    private Slot slot;

    @Transient
    private Rank rank;

    @Column(name = "created_at", insertable = false, updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    protected Reservation() {
    }

    public Reservation(Long id, ReservationName name, Status status, Slot slot) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.slot = slot;
    }

    public static Reservation load(Long id, String name, String status, Slot slot) {
        return new Reservation(id, new ReservationName(name), Status.from(status), slot);
    }

    public static Reservation create(String name, Slot slot) {
        return new Reservation(null, new ReservationName(name), Status.WAITING, slot);
    }

    public Reservation withStatus(Status status) {
        return new Reservation(id, name, status, slot);
    }

    public Reservation withRank(Rank rank) {
        Reservation copy = new Reservation(id, name, status, slot);
        copy.rank = rank;
        return copy;
    }

    public void changeStatus(Status status) {
        this.status = status;
    }

    public void changeSlot(Slot slot) {
        this.slot = slot;
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
            throw new RoomEscapeException(DomainErrorCode.FORBIDDEN, "본인의 예약만 취소할 수 있습니다.");
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

    public Slot getSlot() {
        return slot;
    }

    public Long getSlotId() {
        return slot.getId();
    }

    public Rank getRank() {
        return rank;
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
