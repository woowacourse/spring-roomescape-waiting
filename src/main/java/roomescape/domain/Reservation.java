package roomescape.domain;

import java.time.LocalDate;
import roomescape.exception.reservation.ReservationFieldRequiredException;

public class Reservation {
    private final Long id;
    private final LocalDate date;
    private final ReservationTime time;
    private final Theme theme;
    private final Member member;

    public Reservation(Long id, LocalDate date, ReservationTime time, Theme theme, Member member) {
        validate(date, time, theme, member);
        this.id = id;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.member = member;
    }

    public Reservation(LocalDate date, ReservationTime time, Theme theme, Member member) {
        this(null, date, time, theme, member);
    }

    public Reservation withId(Long id) {
        return new Reservation(id, date, time, theme, member);
    }

    private void validate(LocalDate date, ReservationTime time, Theme theme, Member member) {
        validateDate(date);
        validateTime(time);
        validateTheme(theme);
        validateMember(member);
    }

    private void validateDate(LocalDate date) {
        if (date == null) {
            throw new ReservationFieldRequiredException("날짜");
        }
    }

    private void validateTime(ReservationTime time) {
        if (time == null) {
            throw new ReservationFieldRequiredException("시간");
        }
    }

    private void validateTheme(Theme theme) {
        if (theme == null) {
            throw new ReservationFieldRequiredException("테마");
        }
    }

    private void validateMember(Member member) {
        if (member == null) {
            throw new ReservationFieldRequiredException("멤버");
        }
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

    public Member getMember() {
        return member;
    }
}
