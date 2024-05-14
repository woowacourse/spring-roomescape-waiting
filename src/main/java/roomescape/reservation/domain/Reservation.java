package roomescape.reservation.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import roomescape.global.exception.model.RoomEscapeException;
import roomescape.member.domain.Member;
import roomescape.reservation.exception.ReservationExceptionCode;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.Time;

public class Reservation {

    private long id;
    private final Date date;
    private final Time time;
    private final Theme theme;
    private final Member member;

    public Reservation(long id, Date date, Time time, Theme theme, Member member) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.member = member;
    }

    private Reservation(LocalDate date, long timeId, long themeId, long memberId) {
        this(0, Date.saveDateFrom(date), new Time(timeId), Theme.saveThemeFrom(themeId),
                Member.saveMemberFrom(memberId));
    }

    public static Reservation reservationOf(long id, LocalDate date, Time time, Theme theme, Member member) {
        return new Reservation(id, Date.dateFrom(date), time, theme, member);
    }

    public static Reservation saveReservationOf(LocalDate date, long timeId, long themeId, long memberId) {
        return new Reservation(date, timeId, themeId, memberId);
    }

    public static Reservation saveReservationOf(LocalDate date, Time time, Theme theme, Member member) {
        validateAtSaveDateAndTime(date, time);
        return new Reservation(0, Date.dateFrom(date), time, theme, member);
    }

    public long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date.getDate();
    }

    public Time getReservationTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }

    public Member getMember() {
        return member;
    }

    public void setIdOnSave(long id) {
        this.id = id;
    }

    private static void validateAtSaveDateAndTime(LocalDate date, Time time) {
        if (date.equals(LocalDate.now())) {
            validateTime(time);
        }
    }

    private static void validateTime(Time time) {
        if (time.isBeforeTime(LocalTime.now())) {
            throw new RoomEscapeException(ReservationExceptionCode.RESERVATION_TIME_IS_PAST_EXCEPTION);
        }
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
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
