package roomescape.reservation.controller.dto;

import java.time.LocalDateTime;
import roomescape.reservation.domain.Reservation;
import roomescape.theme.controller.dto.ThemeResponseDto;

public record ReservationSaveResponseDto(
        Long id,
        String name,
        String status,
        LocalDateTime createdAt,
        ReservationTimeResponseDto time,
        ThemeResponseDto theme
) {
    public static ReservationSaveResponseDto from(Reservation reservation) {
        return new ReservationSaveResponseDto(
                reservation.getId(),
                reservation.getName(),
                reservation.getStatus().name(),
                reservation.getCreatedAt(),
                ReservationTimeResponseDto.from(reservation.getTime()),
                ThemeResponseDto.from(reservation.getTheme())
        );
    }
}
