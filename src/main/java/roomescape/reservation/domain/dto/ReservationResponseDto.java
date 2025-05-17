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

