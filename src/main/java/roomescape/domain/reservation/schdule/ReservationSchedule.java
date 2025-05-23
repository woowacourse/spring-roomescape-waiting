package roomescape.domain.reservation.schdule;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import roomescape.domain.theme.Theme;
import roomescape.domain.time.ReservationTime;

@Entity
@Table(name = "reservation_schedule")
public class ReservationSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "time_id")
    private ReservationTime reservationTime;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "theme_id")
    private Theme theme;

    @Embedded
    private ReservationDate reservationDate;

    protected ReservationSchedule() {
    }

    public ReservationSchedule(
            final Long id,
            final ReservationDate reservationDate,
            final ReservationTime reservationTime,
            final Theme theme
    ) {
        this.id = id;
        this.reservationDate = Objects.requireNonNull(reservationDate);
        this.reservationTime = Objects.requireNonNull(reservationTime);
        this.theme = Objects.requireNonNull(theme);
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return reservationDate.date();
    }

    public LocalTime getStartAt() {
        return reservationTime.getStartAt();
    }

    public Theme getTheme() {
        return theme;
    }

    public ReservationTime getReservationTime() {
        return reservationTime;
    }
}
