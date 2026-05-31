package roomescape.reservation.application.dto;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    public Reservation toEntity(ReservationTime time, Theme theme, Clock clock) {
        return Reservation.builder()
                .name(name)
                .date(date)
                .time(time)
                .theme(theme)
                .status(Status.ACTIVE)
                .createdAt(LocalDateTime.now(clock))
                .build();
    }
}
