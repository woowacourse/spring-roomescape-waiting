package roomescape.theme.web;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;

public record PopularThemeRequestDto(
        @Positive(message = "limit는 양수를 입력해 주세요")
        @Max(value = 15)
        Integer limit,
        @Positive(message = "days는 양수를 입력해 주세요") // 0은 허용되지 않는다.
        @Max(value = 10)
        Integer days
) {
    public PopularThemeRequestDto {
        if (limit == null) {
            limit = 10;
        }
        if (days == null) {
            days = 7;
        }
    }
}
