package roomescape.dto.response;

import java.time.LocalDate;
import roomescape.domain.Waiting;

public record WaitingResponse(Long id,
                              String memberName,
                              LocalDate date,
                              ReservationTimeResponse time,
                              String themeName
) {
    public static WaitingResponse from(Waiting waiting) {
        ReservationTimeResponse dto = ReservationTimeResponse.from(waiting.getReservationTime());
        return new WaitingResponse(
                waiting.getId(),
                waiting.getMember().getName(),
                waiting.getDate(),
                dto,
                waiting.getTheme().getName());
    }
}