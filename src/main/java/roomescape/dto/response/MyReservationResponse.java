package roomescape.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.entity.Reservation;
import roomescape.entity.Waiting;

public record MyReservationResponse(
    Long id,
    String theme,
    @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
    @JsonFormat(pattern = "HH:mm") LocalTime time,
    String status) {

    public static MyReservationResponse from(Reservation reservation) {
        return new MyReservationResponse(
            reservation.getId(),
            reservation.getTheme().getName(),
            reservation.getDate(),
            reservation.getTime().getStartAt(),
            reservation.getStatus().getText()
        );
    }

    public static MyReservationResponse from(Waiting waiting, String sequence) {
        return new MyReservationResponse(
            waiting.getId(),
            waiting.getTheme().getName(),
            waiting.getDate(),
            waiting.getTime().getStartAt(),
            sequence + " " + waiting.getStatus().getText()
        );
    }
}
