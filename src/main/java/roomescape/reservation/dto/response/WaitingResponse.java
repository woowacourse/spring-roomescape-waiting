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
        return new WaitingResponse(
                waiting.getId(),
                waiting.getMember().getName(),
                waiting.getReservationTime().getDate(),
                TimeSlotResponse.from(waiting.getReservationTime().getTimeSlot()),
                waiting.getTheme().getName()
        );
    }
}
