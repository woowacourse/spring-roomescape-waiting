package roomescape.dto;

import java.time.LocalDate;
import roomescape.domain.Waiting;

public record WaitingResponseDTO(
        Long id,
        String name,
        LocalDate date,
        ReservationTimeResponseDTO time,
        ThemeResponseDTO theme
) {

    public static WaitingResponseDTO from(Waiting waiting) {
        return new WaitingResponseDTO(
                waiting.getId(),
                waiting.getName(),
                waiting.getDate(),
                ReservationTimeResponseDTO.from(
                        waiting.getTime()
                ),
                ThemeResponseDTO.from(waiting.getTheme())
        );
    }
}
