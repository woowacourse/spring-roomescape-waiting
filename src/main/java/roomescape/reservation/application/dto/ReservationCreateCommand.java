package roomescape.reservation.application.dto;

import java.time.Clock;
import java.time.LocalDate;
import lombok.Builder;
import roomescape.reservation.domain.Reservation;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

@Builder
public record ReservationCreateCommand(
        String name,
        LocalDate date,
        Long timeId,
        Long themeId
) {
    public Reservation toEntity(ReservationTime time, Theme theme, Clock clock) {
        return Reservation.create(name, date, time, theme, clock);
    }
}
