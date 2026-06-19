package roomescape.slot.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;
import lombok.Getter;
import roomescape.exception.ErrorCode;
import roomescape.exception.EscapeRoomException;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

@Getter
public class Slot {
    private final Long id;
    private final LocalDate date;
    private final ReservationTime time;
    private final Theme theme;
    private final int price;

    private Slot(Long id, LocalDate date, ReservationTime time, Theme theme, int price) {
        this.id = id;
        this.date = Objects.requireNonNull(date, "date는 null일 수 없습니다.");
        this.time = Objects.requireNonNull(time, "time은 null일 수 없습니다.");
        this.theme = Objects.requireNonNull(theme, "theme은 null일 수 없습니다.");
        this.price = price;
    }

    public static Slot create(LocalDate date, ReservationTime time, Theme theme) {
        return new Slot(null, date, time, theme, theme.getPrice());
    }

    public static Slot of(Long id, LocalDate date, ReservationTime time, Theme theme) {
        return new Slot(id, date, time, theme, theme.getPrice());
    }

    public static Slot of(Long id, LocalDate date, ReservationTime time, Theme theme, int price) {
        return new Slot(id, date, time, theme, price);
    }

    public Long getTimeId() {
        return time.id();
    }

    public LocalTime getStartAt() {
        return time.startAt();
    }

    public Long getThemeId() {
        return theme.getId();
    }

    public boolean isPast(LocalDateTime now) {
        return LocalDateTime.of(date, time.startAt()).isBefore(now);
    }

    public void validateNotPast(LocalDateTime now) {
        if (isPast(now)) {
            throw new EscapeRoomException(ErrorCode.PAST_SLOT);
        }
    }

}
