package roomescape.repository.reservationwaiting.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import roomescape.domain.reservation.ReservationName;
import roomescape.domain.reservationwaiting.ReservationWaiting;
import roomescape.repository.reservationslot.jpa.ReservationSlotJpaEntity;

@Entity
@Table(
        name = "reservation_waiting",
        uniqueConstraints = @UniqueConstraint(columnNames = {"slot_id", "name"})
)
public class ReservationWaitingJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "slot_id", nullable = false)
    private ReservationSlotJpaEntity slot;

    @Column(nullable = false)
    private String name;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    protected ReservationWaitingJpaEntity() {
    }

    private ReservationWaitingJpaEntity(
            final Long id,
            final ReservationSlotJpaEntity slot,
            final String name,
            final LocalDateTime requestedAt
    ) {
        this.id = id;
        this.slot = slot;
        this.name = name;
        this.requestedAt = requestedAt;
    }

    public static ReservationWaitingJpaEntity from(
            final ReservationWaiting reservationWaiting,
            final ReservationSlotJpaEntity slot
    ) {
        return new ReservationWaitingJpaEntity(
                reservationWaiting.getId(),
                slot,
                reservationWaiting.getName(),
                reservationWaiting.getRequestedAt()
        );
    }

    public ReservationWaiting toDomain() {
        return ReservationWaiting.of(id, slot.toDomain(), name, requestedAt);
    }

    public Long getId() {
        return id;
    }

    public ReservationSlotJpaEntity getSlot() {
        return slot;
    }

    public String getName() {
        return name;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }
}
