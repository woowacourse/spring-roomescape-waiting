package roomescape.waiting.dto;

import java.time.LocalDate;
import roomescape.reservationtime.dto.ReservationTimeResponseDTO;
import roomescape.theme.dto.ThemeResponseDTO;
import roomescape.waiting.domain.Waiting;

public record WaitingResponseDTO(
        Long id,
        String name,
        LocalDate date,
        ReservationTimeResponseDTO time,
        ThemeResponseDTO theme,
        Long waitingNumber
) {

    public static WaitingResponseDTO from(Waiting waiting) {
        return new WaitingResponseDTO(
                waiting.getId(),
                waiting.getName(),
                waiting.getDate(),
                ReservationTimeResponseDTO.from(waiting.getTime()),
                ThemeResponseDTO.from(waiting.getTheme()),
                waiting.getWaitingNumber()
        );
    }

}
