package roomescape.domain.reservation;

import roomescape.domain.member.Member;

import java.time.LocalDate;
import java.time.LocalTime;

public class Reservation {
    private static final long NO_ID = 0;

    private final long id;
    private final Member member;
    private final Schedule schedule;
    private final Theme theme;

    public Reservation(long id, Member member, Schedule schedule, Theme theme) {
        this.id = id;
        this.member = member;
        this.schedule = schedule;
        this.theme = theme;
    }

    public Reservation(long id, Reservation reservation) {
        this(id, reservation.member, reservation.schedule, reservation.theme);
    }

    public Reservation(long id, String date, Member member, ReservationTime reservationTime,
                       final Theme theme) {
        this(id, member, new Schedule(new ReservationDate(date), reservationTime), theme);
    }

    public Reservation(String reservationDate, Member member, ReservationTime reservationTime, Theme theme) {
        this(NO_ID, member, new Schedule(new ReservationDate(reservationDate), reservationTime), theme);
    }

    public long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public LocalDate getDate() {
        return schedule.getDate();
    }

    public LocalTime getTime() {
        return schedule.getTime();
    }

    public ReservationTime getReservationTime() {
        return schedule.getReservationTime();
    }

    public Theme getTheme() {
        return theme;
    }
}
