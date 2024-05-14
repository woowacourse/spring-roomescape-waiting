package roomescape.service.dto.theme;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import roomescape.controller.helper.validator.DateFormatConstraint;

public class PopularThemeRequest {

    @DateFormatConstraint
    private final String startDate;

    @DateFormatConstraint
    private final String endDate;

    @NotNull(message = "불러올 테마 개수는 반드시 입력되어야 합니다.")
    @Positive(message = "불러올 테마 개수는 자연수여야 합니다. ${validatedValue}은 사용할 수 없습니다.")
    @Max(value = 20, message = "불러올 테마 최대 개수는 20개까지 가능합니다.")
    private final Integer count;

    public PopularThemeRequest(String startDate, String endDate, Integer count) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.count = count;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public Integer getCount() {
        return count;
    }
}
