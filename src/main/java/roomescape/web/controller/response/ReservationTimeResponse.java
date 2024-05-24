package roomescape.web.controller.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalTime;

import roomescape.service.response.ReservationTimeDto;

public record ReservationTimeResponse(
        Long id,
        @JsonFormat(pattern = "HH:mm")
        LocalTime startAt) {

    public static ReservationTimeResponse from(ReservationTimeDto appResponse) {
        return new ReservationTimeResponse(appResponse.id(), appResponse.startAt());
    }
}
