package roomescape.domain.reservation;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Embedded
    private ReservationName name;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_id", nullable = false)
    private Slot slot;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;
    @Column(nullable = false)
    private LocalDateTime createdAt;

    private Reservation(Long id, ReservationName name, Slot slot, Status status, LocalDateTime createdAt) {
        this.id = id;
        this.name = Objects.requireNonNull(name);
        this.slot = Objects.requireNonNull(slot);
        this.status = Objects.requireNonNull(status);
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public static Reservation load(Long id, ReservationName reservationName, Slot slot, Status status,
                                   LocalDateTime createdAt) {
        return new Reservation(id, reservationName, slot, status, createdAt);
    }

    public static Reservation create(ReservationName reservationName, Slot slot, Status status, LocalDateTime now) {
        Objects.requireNonNull(now);
        return new Reservation(null, reservationName, slot, status, now);
    }

    public boolean isPastThan(LocalDateTime now) {
        return slot.isBefore(now);
    }

    public void changeTo(Reservation target) {
        this.name = target.name;
        this.slot = target.slot;
        this.status = target.status;
        this.createdAt = target.createdAt;
    }

    public void approve() {
        this.status = Status.APPROVED;
    }

    public boolean isApproved() {
        return status == Status.APPROVED;
    }

    public boolean isEarlierThan(Reservation target) {
        int byTime = createdAt.compareTo(target.getCreatedAt());
        if (byTime != 0) {
            return byTime < 0;
        }
        return id < target.getId();
    }

    public boolean hasSameSlot(Reservation target) {
        return slot.isSame(target.slot);
    }

    public boolean hasSameSlot(Slot target) {
        return slot.isSame(target);
    }

    public boolean hasSameName(ReservationName target) {
        return name.equals(target);
    }
}

