package roomescape.waiting.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import roomescape.reservation.domain.Reservation;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.user.domain.User;

@Entity
public class Waiting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;

    @ManyToOne
    @JoinColumn(name = "reservation_time_id")
    private ReservationTime time;

    @ManyToOne
    @JoinColumn(name = "theme_id")
    private Theme theme;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private User member;

    @ManyToOne
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

    protected Waiting() {
    }

    public Waiting(LocalDate date, ReservationTime time, Theme theme, User member, Reservation reservation) {
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.member = member;
        this.reservation = reservation;
        reservation.addWaiting(this);
    }

    public static Waiting of(LocalDate date, ReservationTime reservationTime, Theme theme, User user, Reservation reservation) {
        return new Waiting(date, reservationTime, theme, user, reservation);
    }

    public boolean isSameMember(User compare) {
        return this.member.equals(compare);
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public ReservationTime getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }

    public User getMember() {
        return member;
    }
}
