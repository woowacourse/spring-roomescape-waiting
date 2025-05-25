package roomescape.dto.time;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
import roomescape.domain.ReservationTime;

public record ReservationTimeResponseDto(
        Long id,
        @JsonFormat(pattern = "HH:mm") LocalTime startAt
) {

    public ReservationTimeResponseDto (ReservationTime reservationTime) {
        this(
                reservationTime.getId(),
                reservationTime.getStartAt()
        );
    }
}
