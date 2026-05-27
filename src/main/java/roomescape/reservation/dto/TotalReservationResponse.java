package roomescape.reservation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.TotalReservation;

public record TotalReservationResponse(
        Long id,
        String name,
        String themeName,
        LocalDate date,
        LocalTime startAt,
        @JsonInclude(Include.NON_NULL)
        Long waitingNumber
) {
    public static TotalReservationResponse from(TotalReservation totalReservation) {
        return new TotalReservationResponse(
                totalReservation.getId(),
                totalReservation.getName(),
                totalReservation.getThemeName(),
                totalReservation.getDate(),
                totalReservation.getStartAt(),
                totalReservation.getWaitingNumber()
        );
    }
}
