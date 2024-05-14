package roomescape.reservation.domain;

import roomescape.member.domain.Member;

import java.time.LocalDate;

public class Reservation {
    private final Long id;
    private final Member member;
    private final LocalDate date;
    private final ReservationTime time;
    private final Theme theme;

    public Reservation(Member member, LocalDate date, ReservationTime time, Theme theme) {
        this(null, member, date, time, theme);
    }

    public Reservation(Long id, Reservation reservation) {
        this(id, reservation.member, reservation.date, reservation.time, reservation.theme);
    }

    public Reservation(Long id, Member member, LocalDate date, ReservationTime time, Theme theme) {
        this.id = id;
        this.member = member;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public boolean isBeforeOrOnToday(LocalDate today) {
        return date.isBefore(today) || date.equals(today);
    }

    public Long getReservationTimeId() {
        return time.getId();
    }

    public Long getThemeId() {
        return theme.getId();
    }

    public Long getMemberId() {
        return member.getId();
    }

    public String getMemberName() {
        return member.getName();
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
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
}
