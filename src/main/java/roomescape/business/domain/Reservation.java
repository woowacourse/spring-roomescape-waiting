package roomescape.business.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_time_id")
    private ReservationTime reservationTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theme_id")
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

    public boolean isSameReservationTime(final ReservationTime reservationTime) {
        return this.reservationTime.isSameReservationTime(reservationTime);
    }

    private void validateDate(final LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("date 필드가 null 입니다.");
        }
    }
}
