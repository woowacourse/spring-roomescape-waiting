package roomescape.domain;

import java.time.LocalDate;

public class Reservation {
    private Long id;
    private LocalDate date;
    private Member member;
    private ReservationTime time;
    private Theme theme;

    public Reservation(Long id, LocalDate date, Member member, ReservationTime time, Theme theme) {
        this.id = id;
        this.date = date;
        this.member = member;
        this.time = time;
        this.theme = theme;
    }

    public Reservation(LocalDate date, Member member, ReservationTime time, Theme theme) {
        this(null, date, member, time, theme);
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

    public ReservationTime getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }
}
