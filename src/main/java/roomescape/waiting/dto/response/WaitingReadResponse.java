package roomescape.waiting.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.waiting.entity.Waiting;

public record WaitingReadResponse(
        Long id,
        String name,
        LocalDate date,
        String theme,
        @JsonFormat(pattern = "HH:mm") LocalTime time
) {
    public static WaitingReadResponse from(Waiting waiting) {
        return new WaitingReadResponse(
                waiting.getId(),
                waiting.getMember().getName(),
                waiting.getReservationSlot().getDate(),
                waiting.getReservationSlot().getTheme().getName(),
                waiting.getReservationSlot().getTime().getStartAt()
        );
    }
}
