package roomescape.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import roomescape.domain.ReservationWaiting;

public record ReservationWaitingResponse(
        Long id,
        String name,
        String theme,
        LocalDate date,
        @JsonFormat(pattern = "HH:mm") LocalTime startAt
) {

    public static ReservationWaitingResponse from(ReservationWaiting waiting) {
        return new ReservationWaitingResponse(
                waiting.getId(),
                waiting.getMemberName(),
                waiting.getThemeName(),
                waiting.getDate(),
                waiting.getStartAt()
        );
    }
}
