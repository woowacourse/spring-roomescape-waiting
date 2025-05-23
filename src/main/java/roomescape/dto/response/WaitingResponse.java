package roomescape.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.entity.WaitingReservation;

public record WaitingResponse (
        Long id,
        String name,
        LocalTime time,
        LocalDate date,
        String themeName
){
    public static WaitingResponse from(WaitingReservation waitingReservation) {

        return new WaitingResponse(
                waitingReservation.getId(),
                waitingReservation.getName(),
                waitingReservation.getStartAt(),
                waitingReservation.getDate(),
                waitingReservation.getThemeName()
        );
    }
}
