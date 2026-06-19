package roomescape.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import roomescape.reservation.Reservation;
import roomescape.time.dto.TimeResponse;

public record ReservationResponse(
        Long id,
        String name,
        Long themeId,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate date,
        String status,
        TimeResponse time) {
    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getName(),
                reservation.getThemeId(),
                reservation.getDate(),
                reservation.getStatus().getDescription(),
                TimeResponse.from(reservation.getTime())
        );
    }
}
