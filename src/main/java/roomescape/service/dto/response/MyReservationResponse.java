package roomescape.service.dto.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.Reservation;

public record MyReservationResponse(
        @NotNull Long id,
        @NotBlank String theme,
        @NotNull LocalDate date,
        @NotNull LocalTime time,
        @NotBlank String status,
        @NotNull Long rank
) {

    public static MyReservationResponse from(Reservation reservation, Long rank) {
        return new MyReservationResponse(
                reservation.getId(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                reservation.getStatus().getName(),
                rank);
    }
}
