package roomescape.controller.dto.request;

import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ThemeFamousFindRequest {
    @Positive(message = "기간은 양수여야 합니다")
    private final Long recentDays;

    private final LocalDate baseDate;

    @Positive(message = "개수는 양수여야 합니다")
    private final Long limit;
}


