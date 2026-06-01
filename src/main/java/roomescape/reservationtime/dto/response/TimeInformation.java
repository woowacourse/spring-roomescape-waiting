package roomescape.reservationtime.dto.response;

import java.time.LocalTime;

public record TimeInformation(
        Long id,
        LocalTime time
) {
}
