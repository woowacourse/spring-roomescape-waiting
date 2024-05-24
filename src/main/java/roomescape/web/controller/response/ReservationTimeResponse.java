package roomescape.web.controller.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalTime;

import roomescape.service.response.ReservationTimeAppResponse;

public record ReservationTimeResponse(
        Long id,
        @JsonFormat(pattern = "HH:mm")
        LocalTime startAt) {

    public static ReservationTimeResponse from(ReservationTimeAppResponse appResponse) {
        return new ReservationTimeResponse(appResponse.id(), appResponse.startAt());
    }
}
