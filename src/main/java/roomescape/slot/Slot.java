package roomescape.slot;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import roomescape.reservationtime.ReservationTime;
import roomescape.theme.Theme;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Slot {
    private final Long id;
    private final LocalDate date;
    private final ReservationTime time;
    private final Theme theme;

    public static Slot create(LocalDate date, ReservationTime time, Theme theme) {
        return new Slot(null, date, time, theme);
    }

    public static Slot of(Long id, LocalDate date, ReservationTime time, Theme theme) {
        return new Slot(id, date, time, theme);
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
}
