package roomescape.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.exception.ReservationErrorCode;
import roomescape.exception.RoomEscapeException;

public class Reservation {

    private static final long NAME_MAX_LENGTH = 20L;
    private final Long id;
    private final String name;
    private final LocalDate date;
    private final ReservationTime time;
    private final Theme theme;

    private Reservation(Long id, String name, LocalDate date, ReservationTime time, Theme theme) {
        validateName(name);
        validateDate(date);
        validateTime(time);
        validateTheme(theme);
        this.id = id;
        this.name = name;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public static Reservation create(String name, LocalDate date, ReservationTime time,
            Theme theme) {
        return new Reservation(null, name, date, time, theme);
    }

    public static Reservation of(Long id, String name, LocalDate date, ReservationTime time,
            Theme theme) {
        validateId(id);
        return new Reservation(id, name, date, time, theme);
    }

    private static void validateId(Long id) {
        if (id == null) {
            throw new IllegalStateException("ID는 필수값입니다.");
        }
        if (id < 1) {
            throw new IllegalStateException("ID는 1 이상의 숫자여야 합니다. (입력값: " + id + ")");
        }
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank() || name.length() > NAME_MAX_LENGTH) {
            throw new RoomEscapeException(ReservationErrorCode.INVALID_NAME);
        }
    }

    private static void validateDate(LocalDate date) {
        if (date == null) {
            throw new RoomEscapeException(ReservationErrorCode.INVALID_DATE);
        }
    }

    private static void validateTime(ReservationTime time) {
        if (time == null) {
            throw new RoomEscapeException(ReservationErrorCode.INVALID_TIME);
        }
    }

    private static void validateTheme(Theme theme) {
        if (theme == null) {
            throw new RoomEscapeException(ReservationErrorCode.INVALID_THEME);
        }
    }

    public void validateNotPastTime(LocalDateTime now) {
        LocalDateTime reservationDateTime = LocalDateTime.of(date, time.getStartAt());

        if (reservationDateTime.isBefore(now)) {
            throw new RoomEscapeException(ReservationErrorCode.RESERVATION_PAST_TIME);
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
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

    public Long getTimeId() {
        return time.getId();
    }

    public Long getThemeId() {
        return theme.getId();
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

    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", date=" + date +
                ", time=" + time +
                ", theme=" + theme +
                '}';
    }
}
