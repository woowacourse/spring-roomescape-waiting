package roomescape.repository.reservation.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationName;
import roomescape.repository.reservationslot.jpa.ReservationSlotJpaEntity;

@Entity
@Table(name = "reservation")
public class ReservationJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "slot_id", nullable = false, unique = true)
    private ReservationSlotJpaEntity slot;

    @Column(name = "created_at", nullable = false, columnDefinition = "timestamp default current_timestamp")
    private LocalDateTime createdAt;

    protected ReservationJpaEntity() {
    }

    private ReservationJpaEntity(
            final Long id,
            final String name,
            final ReservationSlotJpaEntity slot,
            final LocalDateTime createdAt
    ) {
        this.id = id;
        this.name = name;
        this.slot = slot;
        this.createdAt = createdAt;
    }

    public static ReservationJpaEntity from(
            final Reservation reservation,
            final ReservationSlotJpaEntity slot
    ) {
        return new ReservationJpaEntity(
                reservation.getId(),
                reservation.getName(),
                slot,
                reservation.getCreatedAt()
        );
    }

    public Reservation toDomain() {
        return new Reservation(id, ReservationName.from(name), slot.toDomain(), createdAt);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ReservationSlotJpaEntity getSlot() {
        return slot;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setSlot(final ReservationSlotJpaEntity slot) {
        this.slot = slot;
    }
}
