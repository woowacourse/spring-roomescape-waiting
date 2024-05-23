package roomescape.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.Reservation;
import roomescape.domain.Waiting;

public record ReservationMineResponse(long id,
                                      String theme,
                                      LocalDate date,
                                      @JsonFormat(pattern = "HH:mm") LocalTime time,
                                      String status) {

    public static ReservationMineResponse from(Reservation reservation) {
        return new ReservationMineResponse(
                reservation.getId(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                reservation.getStatus().name()
        );
    }

    public static ReservationMineResponse from(Waiting waiting) {
        return new ReservationMineResponse(
                waiting.getId(),
                waiting.getTheme().getName(),
                waiting.getDate(),
                waiting.getTime().getStartAt(),
                waiting.getStatus().name()
        );
    }
}
