package roomescape.reservationtime.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import roomescape.reservationtime.ReservationTime;

import java.time.LocalTime;

public record ReservationTimeSaveRequest(
        @JsonFormat(pattern = "HH:mm") @NotNull LocalTime startAt
) {
    public ReservationTime toDomain() {
        return new ReservationTime(null, this.startAt);
    }
}
