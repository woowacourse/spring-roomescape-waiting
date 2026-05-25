package roomescape.controller.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

public record TimePutRequest(
        @NotNull(message = "시간은 필수입니다.")
        LocalTime startAt
) {
}
