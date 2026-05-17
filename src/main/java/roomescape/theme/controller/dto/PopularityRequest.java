package roomescape.theme.controller.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record PopularityRequest(
        @Min(value = 1, message = "최소 하루 단위로 인기테마를 조회할 수 있습니다.")
        Integer days,
        @Min(value = 1, message = "최소 1개 이상 조회해야 합니다.")
        @Max(value = 10, message = "최대 10개까지 인기테마를 조회할 수 있습니다.")
        Integer size
) {
    public PopularityRequest {
        days = days == null ? 7 : days;
        size = size == null ? 10 : size;
    }
}
