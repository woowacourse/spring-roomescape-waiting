package roomescape.service.dto.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.Reservation;

public record MyReservationResponse(
        @NotNull
        Long reservationId,
        @NotBlank
        String theme,
        @NotNull
        LocalDate date,
        @NotNull
        LocalTime time,
        @NotBlank
        String status
) {

    public static MyReservationResponse from(Reservation reservation, String status) {
        return new MyReservationResponse(
                reservation.getId(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                status);
    }
}
