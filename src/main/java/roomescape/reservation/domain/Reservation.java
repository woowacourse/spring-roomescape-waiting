package roomescape.reservation.domain;


import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import roomescape.member.domain.LoginMember;
import roomescape.theme.domain.Theme;

public class Reservation {

    private final Long id;
    private final LocalDate date;
    private final ReservationTime time;
    private final Theme theme;
    private final LoginMember loginMember;

    public Reservation(Long id, LocalDate date, ReservationTime time, Theme theme, LoginMember loginMember) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.loginMember = loginMember;
    }

    public Reservation(Long id, Reservation reservation) {
        this(id, reservation.date, reservation.time, reservation.theme, reservation.loginMember);
    }

    public Reservation(LocalDate date, ReservationTime time, Theme theme, LoginMember loginMember) {
        this(null, date, time, theme, loginMember);
    }

    public boolean isBeforeNow() {
        return time.isBeforeNow(date);
    }

    public boolean isBetweenInclusive(LocalDate dateFrom, LocalDate dateTo) {
        return !date.isBefore(dateFrom) && !date.isAfter(dateTo);
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getDate(DateTimeFormatter formatter) {
        return date.format(formatter);
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
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
