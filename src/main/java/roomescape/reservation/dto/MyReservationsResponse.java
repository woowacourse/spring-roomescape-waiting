package roomescape.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.MyReservation;

public record MyReservationsResponse(
        String resourceKey,
        Long id,
        String name,
        String themeName,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate date,
        @JsonFormat(pattern = "HH:mm")
        LocalTime startAt,
        String status,
        Long waitingNumber
) {
    public static MyReservationsResponse from(MyReservation myReservation) {
        return new MyReservationsResponse(
                myReservation.getResourceType() + ":" + myReservation.getId(),
                myReservation.getId(),
                myReservation.getName(),
                myReservation.getThemeName(),
                myReservation.getDate(),
                myReservation.getStartAt(),
                myReservation.getStatus(),
                myReservation.getWaitingNumber()
        );
    }
}
