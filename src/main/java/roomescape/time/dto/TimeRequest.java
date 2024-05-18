package roomescape.time.dto;

import java.time.LocalTime;

import jakarta.validation.constraints.NotNull;

public record TimeRequest(
        @NotNull(message = "시간이 선택되지 않았습니다.")
        LocalTime startAt
) {

}
