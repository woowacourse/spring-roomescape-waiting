package roomescape.reservation.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.util.Objects;
import roomescape.theme.domain.Theme;

/***
 * 추후에 사용할 코드입니다.
 */
@Entity
public class ReservationWaitingInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Theme theme;

    @ManyToOne(optional = false)
    private ReservationTime reservationTime;

    private LocalDate date;

    public ReservationWaitingInfo() {}

    public ReservationWaitingInfo(
        Long id, Theme theme,
        ReservationTime reservationTime, LocalDate date) {
        this.id = id;
        this.theme = theme;
        this.reservationTime = reservationTime;
        this.date = date;
    }

    public ReservationWaitingInfo(Theme theme, ReservationTime reservationTime, LocalDate date) {
        this(null, theme, reservationTime, date);
    }

    public Long getId() {
        return id;
    }

    public Theme getTheme() {
        return theme;
    }

    public ReservationTime getReservationTime() {
        return reservationTime;
    }

    public LocalDate getDate() {
        return date;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReservationWaitingInfo that = (ReservationWaitingInfo) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
