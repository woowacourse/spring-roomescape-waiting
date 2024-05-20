package roomescape.service.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record PopularThemeRequest(
        @NotNull(message = "인기 테마를 조회할 시작 날짜가 입력되지 않았어요")
        LocalDate startDate,

        @NotNull(message = "인기 테마를 조회할 종료 날짜가 입력되지 않았어요")
        LocalDate endDate,

        @NotNull(message = "조회할 인기 테마 개수를 입력해주세요")
        @Positive(message = "조회할 인기 테마 개수는 1이상의 양수여야 해요")
        Integer limit
) {
}
