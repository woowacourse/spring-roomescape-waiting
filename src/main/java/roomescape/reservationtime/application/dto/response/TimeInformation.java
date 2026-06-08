package roomescape.reservationtime.application.dto.response;

import java.time.LocalTime;

public record TimeInformation(
        Long id,
        LocalTime time
) {
}
