package roomescape.domain;

import roomescape.common.exception.RoomEscapeException;
import roomescape.common.exception.code.ReservationSlotErrorCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public class ReservationSlot {
    private final Long id;
    private final LocalDate date;
    private final ReservationTime time;
    private final Theme theme;

    public ReservationSlot(Long id, LocalDate date, ReservationTime time, Theme theme) {
        Objects.requireNonNull(date, "예약 날짜는 필수값 입니다.");
        Objects.requireNonNull(time, "예약 시간은 필수값 입니다.");
        Objects.requireNonNull(theme, "테마는 필수값 입니다.");

        this.id = id;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public ReservationSlot(LocalDate date, ReservationTime time, Theme theme) {
        this(null, date, time, theme);
    }

    public static ReservationSlot createWithoutId(LocalDate date, ReservationTime time, Theme theme) {
        return new ReservationSlot(date, time, theme);
    }

    public void validateNotPast(LocalDateTime now) {
        Objects.requireNonNull(now, "현재 시간은 필수값 입니다.");
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

    public Long getId() {
        return id;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        ReservationSlot that = (ReservationSlot) object;
        if (id != null && that.id != null) {
            return Objects.equals(id, that.id);
        }
        return Objects.equals(date, that.date)
                && Objects.equals(time, that.time)
                && Objects.equals(theme, that.theme);
    }

    @Override
    public int hashCode() {
        if (id != null) {
            return Objects.hash(id);
        }
        return Objects.hash(date, time, theme);
    }
}
