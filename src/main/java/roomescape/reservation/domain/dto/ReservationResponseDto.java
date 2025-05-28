package roomescape.reservation.domain.dto;

import java.time.LocalDate;
import roomescape.reservation.domain.Reservation;
import roomescape.reservationtime.domain.dto.ReservationTimeResponseDto;
import roomescape.theme.domain.dto.ThemeResponseDto;
import roomescape.user.domain.dto.UserResponseDto;

public record ReservationResponseDto(
        Long id,
        LocalDate date,
        ReservationTimeResponseDto time,
        ThemeResponseDto theme,
        UserResponseDto user
) {
    public static ReservationResponseDto of(Reservation reservation) {
        ReservationTimeResponseDto reservationTimeResponseDto = ReservationTimeResponseDto.of(
                reservation.getReservationTime());
        ThemeResponseDto themeResponseDto = ThemeResponseDto.of(reservation.getTheme());
        UserResponseDto userResponseDto = UserResponseDto.of(reservation.getUser());

        return new ReservationResponseDto(
                reservation.getId(),
                reservation.getDate(),
                reservationTimeResponseDto,
                themeResponseDto,
                userResponseDto
        );
    }
}

