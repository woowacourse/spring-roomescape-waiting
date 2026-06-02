package roomescape.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.RequestParam;

public record PopularThemeRequestDto(
        @RequestParam(defaultValue = "10")
        @Positive(message = "limit는 양수를 입력해 주세요")
        @Max(value = 15)
        int limit,
        @RequestParam(defaultValue = "7")
        @Positive(message = "days는 양수를 입력해 주세요") // 0은 허용되지 않는다.
        @Max(value = 10)
        int days
) {
}
