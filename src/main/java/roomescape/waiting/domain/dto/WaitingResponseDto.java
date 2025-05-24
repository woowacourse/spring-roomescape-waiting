package roomescape.waiting.domain.dto;

import java.time.LocalDate;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.domain.dto.ReservationTimeResponseDto;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.dto.ThemeResponseDto;
import roomescape.user.domain.dto.UserResponseDto;
import roomescape.waiting.domain.Waiting;

public record WaitingResponseDto(
        Long id,
        LocalDate date,
        ReservationTimeResponseDto time,
        ThemeResponseDto theme,
        UserResponseDto user) {

    public static WaitingResponseDto of(Waiting savedWaiting) {
        ReservationTime reservationTime = savedWaiting.getTime();
        ReservationTimeResponseDto timeDto = ReservationTimeResponseDto.of(reservationTime);
        Theme theme = savedWaiting.getTheme();
        ThemeResponseDto themeDto = ThemeResponseDto.of(theme);
        UserResponseDto userDto = UserResponseDto.of(savedWaiting.getMember());

        return new WaitingResponseDto(savedWaiting.getId(),
                savedWaiting.getDate(),
                timeDto,
                themeDto,
                userDto);
    }
}
