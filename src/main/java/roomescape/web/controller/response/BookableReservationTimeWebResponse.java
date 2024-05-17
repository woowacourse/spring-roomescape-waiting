package roomescape.web.controller.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
import roomescape.service.response.BookableReservationTimeAppResponse;

public record BookableReservationTimeWebResponse(
    Long id,
    @JsonFormat(pattern = "HH:mm") LocalTime startAt,
    boolean alreadyBooked) {

    public static BookableReservationTimeWebResponse from(BookableReservationTimeAppResponse appResponse) {
        return new BookableReservationTimeWebResponse(appResponse.id(), appResponse.startAt(),
            appResponse.alreadyBooked());
    }
}
