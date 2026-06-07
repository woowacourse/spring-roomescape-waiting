package roomescape.controller.dto.request;

import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public class ThemeFamousFindRequest {
    @Positive(message = "기간은 양수여야 합니다")
    private final Long days;

    private final LocalDate date;

    @Positive(message = "개수는 양수여야 합니다")
    private final Long limit;

    public ThemeFamousFindRequest(Long days, LocalDate date, Long limit) {
        this.days = days;
        this.date = date;
        this.limit = limit;
    }

    public Long getDays() {
        return days;
    }

    public LocalDate getDate() {
        return date;
    }

    public Long getLimit() {
        return limit;
    }
}


