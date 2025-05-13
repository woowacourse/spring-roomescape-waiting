package roomescape.reservation.domain;

import roomescape.member.domain.Member;

public class Reservation {

    private final Long id;
    private final Member member;
    private final Theme theme;
    private final ReservationDate date;
    private final ReservationTime reservationTime;

    public Reservation(final Long id, final Member member, final Theme theme, final ReservationDate date,
                       final ReservationTime reservationTime) {
        this.id = id;
        this.member = member;
        this.theme = theme;
        this.date = date;
        this.reservationTime = reservationTime;
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public ReservationDate getDate() {
        return date;
    }

    public ReservationTime getReservationTime() {
        return reservationTime;
    }

    public Theme getTheme() {
        return theme;
    }
}
