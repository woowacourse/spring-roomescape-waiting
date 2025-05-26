package roomescape.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.entity.WaitingReservation;

public record AdminWaitingResponse(
        Long id,
        String name,
        String theme,
        LocalDate date,
        LocalTime startAt
) {

    public static AdminWaitingResponse from(WaitingReservation waitingReservation) {

        return new AdminWaitingResponse(
                waitingReservation.getId(),
                waitingReservation.getName(),
                waitingReservation.getThemeName(),
                waitingReservation.getDate(),
                waitingReservation.getStartAt()
        );
    }

}
