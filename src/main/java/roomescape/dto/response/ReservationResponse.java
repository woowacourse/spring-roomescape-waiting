package roomescape.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

public record ReservationResponse(
    Long id,
    String name,
    @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
    ReservationTime time,
    Theme theme) {

    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
            reservation.getId(),
            reservation.getName(),
            reservation.getDate(),
            reservation.getTime(),
            reservation.getTheme());
    }
}
