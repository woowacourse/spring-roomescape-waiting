package roomescape.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.exception.ReservationErrorCode;
import roomescape.exception.RoomEscapeException;
import roomescape.exception.WaitingErrorCode;

public class Waiting {

    private static final long NAME_MAX_LENGTH = 20L;
    private final Long id;
    private final String name;
    private final LocalDate date;
    private final ReservationTime time;
    private final Theme theme;
    private final Long waitingNumber;

    private Waiting(Long id, String name, LocalDate date, ReservationTime time, Theme theme,
            Long waitingNumber) {
        validateName(name);
        validateDate(date);
        validateTime(time);
        validateTheme(theme);
        validateWaitingNumber(waitingNumber);
        this.id = id;
        this.name = name;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.waitingNumber = waitingNumber;
    }

    public static Waiting create(String name, LocalDate date, ReservationTime time,
            Theme theme, Long waitingNumber) {
        return new Waiting(null, name, date, time, theme, waitingNumber);
    }

    public static Waiting of(Long id, String name, LocalDate date, ReservationTime time,
            Theme theme, Long waitingNumber) {
        validateId(id);
        return new Waiting(id, name, date, time, theme, waitingNumber);
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

    private static void validateWaitingNumber(Long waitingNumber) {
        if (waitingNumber == null) {
            throw new RoomEscapeException(WaitingErrorCode.INVALID_WAITING_NUMBER);
        }
    }

    public void validateNotPastTime(LocalDateTime now) {
        LocalDateTime waitingDateTime = LocalDateTime.of(date, time.getStartAt());

        if (waitingDateTime.isBefore(now)) {
            throw new RoomEscapeException(WaitingErrorCode.WAITING_PAST_TIME);
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

    public Long getWaitingNumber() {
        return waitingNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Waiting that = (Waiting) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Waiting{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", date=" + date +
                ", time=" + time +
                ", theme=" + theme +
                ", waitingNumber=" + waitingNumber +
                '}';
    }
}
