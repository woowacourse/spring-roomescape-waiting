package roomescape.reservation.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.application.dto.response.MyWaitingServiceResponse;

public record MyWaitingResponse(
    Long id,
    String name,
    String theme,
    LocalDate date,
    @JsonFormat(pattern = "HH:mm") LocalTime startAt
) {

    public static MyWaitingResponse from(MyWaitingServiceResponse response) {
        return new MyWaitingResponse(
            response.waitingId(),
            response.memberName(),
            response.themeName(),
            response.date(),
            response.time()
        );
    }
}

