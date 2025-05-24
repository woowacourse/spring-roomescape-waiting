package roomescape.domain.reservation;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.util.Objects;
import roomescape.domain.theme.Theme;
import roomescape.domain.time.ReservationTime;

@Embeddable
public class ReservationSchedule {

    @Embedded
    private ReservationDate reservationDate;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "time_id")
    private ReservationTime reservationTime;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Theme theme;

    protected ReservationSchedule() {
    }

    public ReservationSchedule(
            final ReservationDate reservationDate,
            final ReservationTime reservationTime,
            final Theme theme
    ) {
        this.reservationDate = Objects.requireNonNull(reservationDate);
        this.reservationTime = Objects.requireNonNull(reservationTime);
        this.theme = Objects.requireNonNull(theme);
    }

    public ReservationDate getReservationDate() {
        return reservationDate;
    }

    public ReservationTime getReservationTime() {
        return reservationTime;
    }

    public Theme getTheme() {
        return theme;
    }

    public Long getTimeId() {
        return reservationTime.getId();
    }
}
