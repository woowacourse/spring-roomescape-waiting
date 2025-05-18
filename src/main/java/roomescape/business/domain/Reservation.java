package roomescape.business.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "reservations")
public class Reservation {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    private ReservationTime reservationTime;

    @ManyToOne(fetch = FetchType.LAZY)
    private Theme theme;

    public Reservation(final Long id, final LocalDate date, final Member member, final ReservationTime reservationTime,
                       final Theme theme
    ) {
        validateDate(date);

        this.id = id;
        this.date = date;
        this.member = member;
        this.reservationTime = reservationTime;
        this.theme = theme;
    }

    public Reservation(final LocalDate date, final Member member, final ReservationTime reservationTime, final Theme theme) {
        this(null, date, member, reservationTime, theme);
    }

    public Reservation() {

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

    public Member getMember() {
        return member;
    }

    public ReservationTime getReservationTime() {
        return reservationTime;
    }

    public Theme getTheme() {
        return theme;
    }

    private void validateDate(final LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("date 필드가 null 입니다.");
        }
    }
}
