package roomescape.service.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.entity.Waiting;

public record WaitingResponse(Long id, String name, LocalDate date, LocalTime time, String theme) {

    public static WaitingResponse from(Waiting waiting) {
        return new WaitingResponse(
                waiting.getId(),
                waiting.getMember().getName(),
                waiting.getReservation().getDate(),
                waiting.getReservation().getTime().getStartAt(),
                waiting.getReservation().getTheme().getName()
        );
    }
}
