package roomescape.reservation.controller.dto;

import java.time.LocalDateTime;
import roomescape.reservation.domain.Reservation;
import roomescape.theme.controller.dto.ThemeResponse;

public record ReservationSaveResponse(
        Long id,
        String name,
        String status,
        LocalDateTime createdAt,
        ReservationTimeResponse time,
        ThemeResponse theme
) {
    public static ReservationSaveResponse from(Reservation reservation) {
        return new ReservationSaveResponse(
                reservation.getId(),
                reservation.getName(),
                reservation.getStatus().name(),
                reservation.getCreatedAt(),
                ReservationTimeResponse.from(reservation.getTime()),
                ThemeResponse.from(reservation.getTheme())
        );
    }
}
