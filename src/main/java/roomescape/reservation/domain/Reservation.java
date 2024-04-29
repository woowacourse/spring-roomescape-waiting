package roomescape.reservation.domain;

import java.time.LocalDate;
import java.util.Objects;
import roomescape.member.domain.Member;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

public class Reservation {

    private final Long id;
    private final Member member;
    private final ReservationDate date;
    private final ReservationTime time;
    private final Theme theme;

    public Reservation(Long id, Member member, LocalDate date, ReservationTime time, Theme theme) {
        this.id = id;
        this.member = member;
        this.date = new ReservationDate(date);
        this.time = time;
        this.theme = theme;
    }

    public Reservation(Long id, Reservation reservation) {
        this(id, reservation.getMember(), reservation.getDate(), reservation.getTime(), reservation.getTheme());
    }

    public boolean isDateBefore(LocalDate target) {
        return date.isBefore(target);
    }

    public Long getId() {
        return id;
    }

    public Long getMemberId() {
        return member.getId();
    }

    public Long getTimeId() {
        return time.getId();
    }

    public Long getThemeId() {
        return theme.getId();
    }

    public Member getMember() {
        return member;
    }

    public String getName() {
        return member.getName();
    }

    public LocalDate getDate() {
        return date.getDate();
    }

    public ReservationTime getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Reservation that = (Reservation) o;
        return Objects.equals(id, that.id) && Objects.equals(member, that.member)
                && Objects.equals(date, that.date) && Objects.equals(time, that.time)
                && Objects.equals(theme, that.theme);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, member, date, time, theme);
    }
}
