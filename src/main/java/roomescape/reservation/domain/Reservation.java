package roomescape.reservation.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.member.domain.Member;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private ReservationDate reservationDate;

    @ManyToOne
    private ReservationTime reservationTime;

    @ManyToOne
    private Theme theme;

    @ManyToOne
    private Member member;

    public Reservation() {

    }

    public Reservation(
            Long id,
            LocalDate reservationDate,
            ReservationTime reservationTime,
            Theme theme,
            Member member
    ) {
        this.id = id;
        this.reservationDate = new ReservationDate(reservationDate);
        this.reservationTime = reservationTime;
        this.theme = theme;
        this.member = member;
    }

    public static Reservation create(
            LocalDate reservationDate,
            ReservationTime reservationTime,
            Theme theme,
            Member member
    ) {
        return new Reservation(null, reservationDate, reservationTime, theme, member);
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return reservationDate.getDate();
    }

    public LocalTime getStartAt() {
        return reservationTime.getStartAt();
    }

    public ReservationTime getReservationTime() {
        return reservationTime;
    }

    public Long getTimeId() {
        return reservationTime.getId();
    }

    public Theme getTheme() {
        return theme;
    }

    public Member getMember() {
        return member;
    }
}
