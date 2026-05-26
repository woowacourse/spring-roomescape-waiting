package roomescape.reservation.application.dto;

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
    public Reservation toEntity(final ReservationTime time, final Theme theme) {
        return Reservation.builder()
                .name(this.name)
                .date(this.date)
                .time(time)
                .theme(theme)
                .status(Status.ACTIVE)
                .build();
    }
}
