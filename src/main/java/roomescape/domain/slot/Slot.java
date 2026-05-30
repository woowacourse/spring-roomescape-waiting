package roomescape.domain.slot;

import java.time.LocalDate;
import roomescape.domain.slot.theme.Theme;
import roomescape.domain.slot.time.ReservationTime;

public record Slot(
        LocalDate date,
        ReservationTime time,
        Theme theme
) {

    public static Slot from(LocalDate date, ReservationTime time, Theme theme) {
        return new Slot(date, time, theme);
    }
}
