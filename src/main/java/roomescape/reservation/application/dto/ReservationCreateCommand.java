package roomescape.reservation.application.dto;

import java.time.Clock;
import java.time.LocalDate;
import lombok.Builder;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Status;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

@Builder
public record ReservationCreateCommand(
        String name,
        LocalDate date,
        Long timeId,
        Long themeId
) {
    public Reservation toEntity(ReservationTime time, Theme theme, Status status, Clock clock) {
        return Reservation.create(name, date, time, theme, status, clock);
    }
}
