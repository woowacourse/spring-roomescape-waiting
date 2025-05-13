package roomescape.reservation;

import java.time.LocalDate;
import roomescape.member.Member;
import roomescape.reservationtime.ReservationTime;
import roomescape.theme.Theme;

public class Reservation {

    private final Long id;
    private final LocalDate date;
    private final Member member;
    private final ReservationTime reservationTime;
    private final Theme theme;

    public Reservation(final Long id, final LocalDate date, final Member member, final ReservationTime reservationTime,
                       final Theme theme) {
        this.id = id;
        this.date = date;
        this.member = member;
        this.reservationTime = reservationTime;
        this.theme = theme;
    }

    public Reservation(final LocalDate date) {
        this(null, date, null, null, null);
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
}
