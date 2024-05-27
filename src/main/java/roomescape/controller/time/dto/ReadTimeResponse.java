package roomescape.controller.time.dto;

import java.time.format.DateTimeFormatter;
import roomescape.domain.ReservationTime;

public record ReadTimeResponse(Long id, String startAt) {

    public static ReadTimeResponse from(final ReservationTime time) {
        return new ReadTimeResponse(
                time.getId(),
                time.getStartAt().format(DateTimeFormatter.ofPattern("HH:mm"))
        );
    }
}
