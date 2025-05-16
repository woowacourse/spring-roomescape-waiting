package roomescape.dto.response;

import java.time.LocalDate;
import roomescape.domain.Reservation;

public record ReservationResponseDto(
        Long id,
        LocalDate date,
        ReservationTimeResponseDto time,
        ThemeResponseDto theme,
        UserResponseDto user
) {

    public static ReservationResponseDto from(Reservation reservation,
            ReservationTimeResponseDto reservationTimeResponseDto,
            ThemeResponseDto themeResponseDto,
            UserResponseDto userResponseDto) {
        return new ReservationResponseDto(
                reservation.getId(),
                reservation.getDate(),
                reservationTimeResponseDto,
                themeResponseDto,
                userResponseDto
        );
    }
}

