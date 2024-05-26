package roomescape.reservation.service.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.controller.dto.WaitingResponse;
import roomescape.reservation.domain.repository.dto.MyReservationProjection;

public record MyReservationInfo(long id,
                                String themeName,
                                LocalDate date,
                                LocalTime time,
                                WaitingResponse waitingResponse) {
    public static MyReservationInfo of(MyReservationProjection projectionInterface) {
        return new MyReservationInfo(
                projectionInterface.getId(),
                projectionInterface.getThemeName(),
                projectionInterface.getDate(),
                projectionInterface.getTime(),
                new WaitingResponse(projectionInterface.getStatus(), projectionInterface.getWaitingNumber())
        );
    }
}
