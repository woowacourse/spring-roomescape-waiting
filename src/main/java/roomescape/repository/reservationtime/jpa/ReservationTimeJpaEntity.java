package roomescape.repository.reservationtime.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalTime;
import roomescape.domain.reservationtime.ReservationTime;

@Entity
@Table(name = "reservation_time")
public class ReservationTimeJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "start_at", nullable = false, unique = true)
    private LocalTime startAt;

    protected ReservationTimeJpaEntity() {
    }

    private ReservationTimeJpaEntity(final Long id, final LocalTime startAt) {
        this.id = id;
        this.startAt = startAt;
    }

    public static ReservationTimeJpaEntity from(final ReservationTime reservationTime) {
        return new ReservationTimeJpaEntity(reservationTime.getId(), reservationTime.getStartAt());
    }

    public ReservationTime toDomain() {
        return ReservationTime.of(id, startAt);
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }
}
