package roomescape.reservation.controller.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.service.dto.MyReservationInfo;

public record MyReservationResponse(long id, String themeName, LocalDate date, LocalTime time,
                                    String status) {
    public static MyReservationResponse from(MyReservationInfo myReservationInfo) {
        return new MyReservationResponse(
                myReservationInfo.id(),
                myReservationInfo.themeName(),
                myReservationInfo.date(),
                myReservationInfo.time(),
                myReservationInfo.waitingResponse().getStatus()
        );
    }
}
