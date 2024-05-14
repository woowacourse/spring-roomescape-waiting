package roomescape.service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import roomescape.domain.ReservationTime;

public record ReservationTimeSaveRequest(
        @NotNull
        @JsonFormat(shape = Shape.STRING, pattern = "HH:mm", timezone = "Asia/Seoul")
        LocalTime startAt
) {

    public ReservationTime toReservationTime() {
        return new ReservationTime(startAt);
    }
}
