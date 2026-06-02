package roomescape.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.MyReservation;

public record MyReservationResponse(
        String resourceKey,
        Long id,
        String name,
        String themeName,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate date,
        @JsonFormat(pattern = "HH:mm")
        LocalTime startAt,
        String status,
        @JsonInclude(Include.NON_NULL)
        Long waitingNumber
) {
    public static MyReservationResponse from(MyReservation myReservation) {
        return new MyReservationResponse(
                myReservation.resourceType() + ":" + myReservation.id(),
                myReservation.id(),
                myReservation.name(),
                myReservation.themeName(),
                myReservation.date(),
                myReservation.startAt(),
                myReservation.status(),
                myReservation.waitingNumber()
        );
    }
}
