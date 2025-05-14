package roomescape.domain.reservation.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.reservation.application.dto.response.ReservationServiceResponse;

//TODO : 요구사항이 변경된다면, memberId까지 반환
public record ReservationResponse(
        Long id,
        String name,
        LocalDate date,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
        LocalTime startAt,
        String themeName
) {

    public static ReservationResponse from(ReservationServiceResponse response) {
        return new ReservationResponse(
                response.id(),
                response.name(),
                response.date(),
                response.startAt(),
                response.themeName()
        );
    }
}
