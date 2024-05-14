package roomescape.web.controller.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
import roomescape.service.response.ReservationTimeAppResponse;

public record ReservationTimeWebResponse(
    Long id,
    @JsonFormat(pattern = "HH:mm")
    LocalTime startAt) {

    public static ReservationTimeWebResponse from(ReservationTimeAppResponse appResponse) {
        return new ReservationTimeWebResponse(appResponse.id(), appResponse.startAt());
    }
}
