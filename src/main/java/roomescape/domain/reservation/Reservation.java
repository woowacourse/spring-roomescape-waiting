package roomescape.domain.reservation;

import java.time.LocalDate;
import java.util.Objects;
import roomescape.domain.member.LoginMember;
import roomescape.domain.theme.Theme;

public class Reservation {

    private final Long id;
    private final LoginMember loginMember;
    private final Date date;
    private final ReservationTime time;
    private final Theme theme;

    public Reservation(LoginMember loginMember, String rawDate, ReservationTime time, Theme theme) {
        this(null, loginMember, rawDate, time, theme);
    }

    public Reservation(Long id, LoginMember loginMember, String rawDate, ReservationTime time, Theme theme) {
        this.id = id;
        this.loginMember = loginMember;
        this.date = new Date(rawDate);
        this.time = time;
        this.theme = theme;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return loginMember.getName();
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

    public LoginMember getLoginMember() {
        return loginMember;
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
        return Objects.equals(id, that.id) && Objects.equals(loginMember, that.loginMember)
            && Objects.equals(date, that.date) && Objects.equals(time, that.time)
            && Objects.equals(theme, that.theme);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, loginMember, date, time, theme);
    }
}
