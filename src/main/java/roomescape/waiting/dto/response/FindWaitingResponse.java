package roomescape.waiting.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.waiting.model.Waiting;

public record FindWaitingResponse(Long id,
                                  String name,
                                  LocalDate date,
                                  LocalTime time,
                                  String theme) {
    public static FindWaitingResponse from(final Waiting waiting) {
        return new FindWaitingResponse(
                waiting.getId(),
                waiting.getMember().getName(),
                waiting.getReservation().getDate(),
                waiting.getReservation().getReservationTime().getStartAt(),
                waiting.getReservation().getTheme().getName());
    }
}
