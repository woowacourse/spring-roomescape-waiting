package roomescape.waiting.domain.dto;

import java.time.LocalDate;
import roomescape.reservationtime.domain.dto.ReservationTimeResponseDto;
import roomescape.theme.domain.dto.ThemeResponseDto;
import roomescape.waiting.domain.Waiting;

public record WaitingResponseDto(
        Long id,
        LocalDate date,
        ReservationTimeResponseDto timeDto,
        ThemeResponseDto themeDto) {

    public static WaitingResponseDto of(Waiting waiting,
                                        ReservationTimeResponseDto timeDto,
                                        ThemeResponseDto themeDto) {
        return new WaitingResponseDto(waiting.getId(),
                waiting.getDate(),
                timeDto,
                themeDto);
    }
}
