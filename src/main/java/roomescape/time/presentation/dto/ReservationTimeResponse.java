package roomescape.time.presentation.dto;

import java.time.LocalTime;
import lombok.Builder;
import roomescape.time.application.dto.ReservationTimeInfo;

@Builder
public record ReservationTimeResponse(
        Long id,
        LocalTime startAt
) {
    public static ReservationTimeResponse from(final ReservationTimeInfo reservationTime) {
        return ReservationTimeResponse.builder()
                .id(reservationTime.id())
                .startAt(reservationTime.startAt())
                .build();
    }
}
