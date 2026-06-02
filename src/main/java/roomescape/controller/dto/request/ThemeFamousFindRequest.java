package roomescape.controller.dto.request;

import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public class ThemeFamousFindRequest {
    @Positive(message = "기간은 양수여야 합니다")
    private final Long recentDays;

    private final LocalDate baseDate;

    @Positive(message = "개수는 양수여야 합니다")
    private final Long limit;

    public ThemeFamousFindRequest(Long recentDays, LocalDate baseDate, Long limit) {
        this.recentDays = recentDays;
        this.baseDate = baseDate;
        this.limit = limit;
    }

    public Long getRecentDays() {
        return recentDays;
    }

    public LocalDate getBaseDate() {
        return baseDate;
    }

    public Long getLimit() {
        return limit;
    }
}


