package roomescape.waiting.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.waiting.entity.Waiting;

public record WaitingCreateResponse(
        Long waitingId,
        LocalDate date,
        String theme,
        @JsonFormat(pattern = "HH:mm") LocalTime time,
        Long rank
) {
    public static WaitingCreateResponse from(Waiting waiting, Long rank) {
        return new WaitingCreateResponse(
                waiting.getId(),
                waiting.getReservationSlot().getDate(),
                waiting.getReservationSlot().getTheme().getName(),
                waiting.getReservationSlot().getTime().getStartAt(),
                rank
        );
    }
}
