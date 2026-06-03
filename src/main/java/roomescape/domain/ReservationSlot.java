package roomescape.domain;

import roomescape.common.exception.RoomEscapeException;
import roomescape.common.exception.code.ReservationSlotErrorCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public class ReservationSlot {
    private final LocalDate date;
    private final ReservationTime time;
    private final Theme theme;

    public ReservationSlot(LocalDate date, ReservationTime time, Theme theme) {
        Objects.requireNonNull(date, "예약 날짜는 필수값 입니다.");
        Objects.requireNonNull(time, "예약 시간은 필수값 입니다.");
        Objects.requireNonNull(theme, "테마는 필수값 입니다.");

        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public void validateNotPast(LocalDateTime now) {
        if (time.isPast(date, now)) {
            throw new RoomEscapeException(ReservationSlotErrorCode.PAST_DATETIME);
        }
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

    public long getTimeId() {
        return time.getId();
    }

    public long getThemeId() {
        return theme.getId();
    }
}
