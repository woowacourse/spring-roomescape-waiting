package roomescape.business.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @ManyToOne
    @JoinColumn(name = "reservation_time_id")
    private ReservationTime reservationTime;

    @ManyToOne
    @JoinColumn(name = "theme_id")
    private Theme theme;

    public Reservation(final LocalDate date, final ReservationTime reservationTime, final Theme theme) {
        validateDate(date);

        this.date = date;
        this.reservationTime = reservationTime;
        this.theme = theme;
    }

    protected Reservation() {
    }

    private void validateDate(final LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("date 필드가 null 입니다.");
        }
    }

    public boolean isSameReservationTime(final ReservationTime reservationTime) {
        return this.reservationTime.isSameReservationTime(reservationTime);
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public ReservationTime getReservationTime() {
        return reservationTime;
    }

    public Theme getTheme() {
        return theme;
    }
}
