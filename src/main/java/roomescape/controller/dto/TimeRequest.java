package roomescape.controller.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

public record TimeRequest(
        @NotNull(message = "시간대가 입력되지 않았습니다. 시간을 입력해주세요.")
        LocalTime startAt
) {
}
