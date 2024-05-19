package roomescape.controller.time.dto;

import roomescape.domain.ReservationTime;

import java.time.format.DateTimeFormatter;

public record ReadTimeResponse(Long id, String startAt) {

    public static ReadTimeResponse from(final ReservationTime time) {
        return new ReadTimeResponse(
                time.getId(),
                time.getStartAt().format(DateTimeFormatter.ofPattern("HH:mm"))
        );
    }
}
