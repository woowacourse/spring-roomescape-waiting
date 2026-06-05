package roomescape.dto;

import java.time.LocalDate;
import roomescape.domain.Waiting;

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
                waiting.getReservationSlot().getDate(),
                ReservationTimeResponseDTO.from(
                        waiting.getReservationSlot().getTime()
                ),
                ThemeResponseDTO.from(waiting.getReservationSlot().getTheme()),
                waiting.getWaitingNumber()
        );
    }

}
