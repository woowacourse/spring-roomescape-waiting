package roomescape.domain;

import static roomescape.exception.ExceptionType.EMPTY_DATE;
import static roomescape.exception.ExceptionType.EMPTY_MEMBER;
import static roomescape.exception.ExceptionType.EMPTY_THEME;
import static roomescape.exception.ExceptionType.EMPTY_TIME;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;
import roomescape.exception.RoomescapeException;

public class Reservation implements Comparable<Reservation> {
    private final Long id;
    private final LocalDate date;
    private final ReservationTime time;
    private final Theme theme;
    private final LoginMember member;

    public Reservation(long id, Reservation reservationBeforeSave) {
        this(id,
                reservationBeforeSave.date,
                reservationBeforeSave.time,
                reservationBeforeSave.theme,
                reservationBeforeSave.member);
    }

    public Reservation(LocalDate date, ReservationTime time, Theme theme, LoginMember member) {
        this(null, date, time, theme, member);
    }

    public Reservation(Long id, LocalDate date, ReservationTime time, Theme theme, LoginMember member) {
        validateDate(date);
        validateTime(time);
        validateTheme(theme);
        validateMember(member);
        this.id = id;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.member = member;
    }

    private void validateTheme(Theme theme) {
        if (theme == null) {
            throw new RoomescapeException(EMPTY_THEME);
        }
    }

    private void validateTime(ReservationTime time) {
        if (time == null) {
            throw new RoomescapeException(EMPTY_TIME);
        }
    }

    private void validateDate(LocalDate date) {
        if (date == null) {
            throw new RoomescapeException(EMPTY_DATE);
        }
    }

    private void validateMember(LoginMember member) {
        if (member == null) {
            throw new RoomescapeException(EMPTY_MEMBER);
        }
    }

    public boolean isBefore(LocalDateTime base) {
        return this.getLocalDateTime().isBefore(base);
    }

    public boolean isBetween(Duration duration) {
        return duration.contains(date);
    }

    public boolean hasSameId(long id) {
        return this.id == id;
    }

    public boolean isReservationTimeOf(long id) {
        return this.time.isIdOf(id);
    }

    public boolean isDateOf(LocalDate date) {
        return this.date.equals(date);
    }

    public boolean isThemeOf(long id) {
        return this.theme.isIdOf(id);
    }

    public boolean isSameReservation(Reservation beforeSave) {
        return this.getLocalDateTime().equals(beforeSave.getLocalDateTime())
                && this.isSameTheme(beforeSave);
    }

    public boolean isSameTheme(Reservation reservation) {
        return this.theme.equals(reservation.theme);
    }

    @Override
    public int compareTo(Reservation other) {
        LocalDateTime dateTime = LocalDateTime.of(date, time.getStartAt());
        LocalDateTime otherDateTime = LocalDateTime.of(other.date, other.time.getStartAt());
        return dateTime.compareTo(otherDateTime);
    }

    public long getId() {
        return id;
    }

    public LoginMember getMember() {
        return member;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalDateTime getLocalDateTime() {
        return LocalDateTime.of(this.date, this.getTime());
    }

    public ReservationTime getReservationTime() {
        return time;
    }

    public LocalTime getTime() {
        return time.getStartAt();
    }

    public Theme getTheme() {
        return theme;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (member != null ? member.hashCode() : 0);
        result = 31 * result + (date != null ? date.hashCode() : 0);
        result = 31 * result + (time != null ? time.hashCode() : 0);
        return result;
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
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", date=" + date +
                ", time=" + time +
                ", theme=" + theme +
                ", member=" + member +
                '}';
    }
}
