package roomescape.controller.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

public record TimeRequest(
        @NotNull(message = "시간대는 필수입니다.")
        LocalTime startAt
) {
}
