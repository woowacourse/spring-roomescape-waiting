package roomescape.repository.reservationslot.jpa;

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
import java.time.LocalDate;
import roomescape.domain.reservationslot.ReservationSlot;
import roomescape.repository.reservationtime.jpa.ReservationTimeJpaEntity;
import roomescape.repository.theme.jpa.ThemeJpaEntity;

@Entity
@Table(
        name = "reservation_slot",
        uniqueConstraints = @UniqueConstraint(columnNames = {"date", "theme_id", "time_id"})
)
public class ReservationSlotJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "theme_id", nullable = false)
    private ThemeJpaEntity theme;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "time_id", nullable = false)
    private ReservationTimeJpaEntity time;

    protected ReservationSlotJpaEntity() {
    }

    private ReservationSlotJpaEntity(
            final Long id,
            final LocalDate date,
            final ThemeJpaEntity theme,
            final ReservationTimeJpaEntity time
    ) {
        this.id = id;
        this.date = date;
        this.theme = theme;
        this.time = time;
    }

    public static ReservationSlotJpaEntity from(
            final ReservationSlot reservationSlot,
            final ThemeJpaEntity theme,
            final ReservationTimeJpaEntity time
    ) {
        return new ReservationSlotJpaEntity(
                reservationSlot.getId(),
                reservationSlot.getDate(),
                theme,
                time
        );
    }

    public ReservationSlot toDomain() {
        return new ReservationSlot(
                id,
                date,
                theme.toDomain(),
                time.toDomain()
        );
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public ThemeJpaEntity getTheme() {
        return theme;
    }

    public ReservationTimeJpaEntity getTime() {
        return time;
    }
}
