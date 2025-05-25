package roomescape.waiting.domain;

import java.time.LocalDate;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

public record WaitingSlot(
        Theme theme,
        ReservationTime time,
        LocalDate date
) {
    public static WaitingSlot from(Waiting waiting) {
        return new WaitingSlot(waiting.getTheme(), waiting.getTime(), waiting.getDate());
    }
}