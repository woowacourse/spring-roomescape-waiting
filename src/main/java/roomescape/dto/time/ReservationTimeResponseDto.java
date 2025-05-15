package roomescape.dto.time;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
import roomescape.domain.ReservationTime;

public record ReservationTimeResponseDto(long id, @JsonFormat(pattern = "HH:mm") LocalTime startAt) {

    public static ReservationTimeResponseDto from(ReservationTime reservationTime) {
        return new ReservationTimeResponseDto(reservationTime.getId(), reservationTime.getStartAt());
    }
}
