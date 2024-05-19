package roomescape.service.dto.time;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import roomescape.controller.helper.validator.DateFormatConstraint;

public class AvailableTimeRequest {

    @DateFormatConstraint
    private final String date;

    @NotNull(message = "테마 아이디는 반드시 입력되어야 합니다.")
    @Positive(message = "테마 아이디는 자연수여야 합니다. ${validatedValue}은 사용할 수 없습니다.")
    private final Long themeId;

    public AvailableTimeRequest(String date, Long themeId) {
        this.date = date;
        this.themeId = themeId;
    }

    public String getDate() {
        return date;
    }

    public Long getThemeId() {
        return themeId;
    }
}
