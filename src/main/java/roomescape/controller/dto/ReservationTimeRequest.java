package roomescape.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import roomescape.service.dto.ReservationTimeCreateCommand;

public record ReservationTimeRequest(
        @NotNull(message = "예약 시간은 필수입니다")
        @JsonFormat(pattern = "HH:mm") LocalTime startAt
) {
    public ReservationTimeCreateCommand toCommand() {
        return new ReservationTimeCreateCommand(startAt);
    }
}