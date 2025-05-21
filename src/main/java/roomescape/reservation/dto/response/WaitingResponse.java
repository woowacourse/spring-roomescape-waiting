package roomescape.reservation.dto.response;

import java.time.LocalDate;
import roomescape.reservation.domain.Waiting;

public record WaitingResponse(
        Long id,
        String memberName,
        LocalDate date,
        TimeSlotResponse time,
        String themeName
) {
    public static WaitingResponse from(Waiting waiting) {
        TimeSlotResponse dto = TimeSlotResponse.from(waiting.getTimeSlot());
        return new WaitingResponse(waiting.getId(), waiting.getMember().getName(), waiting.getDate(),
                dto,
                waiting.getTheme().getName());
    }
}
