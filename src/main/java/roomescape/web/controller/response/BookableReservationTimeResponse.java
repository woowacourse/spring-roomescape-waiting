package roomescape.web.controller.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalTime;

import roomescape.service.response.BookableReservationTimeDto;

public record BookableReservationTimeResponse(
        Long id,
        @JsonFormat(pattern = "HH:mm") LocalTime startAt,
        boolean alreadyBooked) {

    public static BookableReservationTimeResponse from(BookableReservationTimeDto appResponse) {
        return new BookableReservationTimeResponse(appResponse.id(), appResponse.startAt(),
                appResponse.alreadyBooked());
    }
}
