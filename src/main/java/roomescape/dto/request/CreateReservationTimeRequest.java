package roomescape.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import roomescape.entity.ReservationTime;

public record CreateReservationTimeRequest(
        @JsonFormat(pattern = "HH:mm")
        @NotNull(message = "시간은 비어있을 수 없습니다.")
        LocalTime startAt
) {

    public ReservationTime toReservationTime() {
        return new ReservationTime(startAt);
    }
}

