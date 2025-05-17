package roomescape.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalTime;

public record AvailableReservationTimeResponse(
        Long id,

        @JsonFormat(pattern = "HH:mm")
        LocalTime startAt,

        boolean isReserved) {
}
