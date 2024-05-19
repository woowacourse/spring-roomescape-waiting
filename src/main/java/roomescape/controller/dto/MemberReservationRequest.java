package roomescape.controller.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import roomescape.controller.helper.validator.DateFormatConstraint;

public class MemberReservationRequest {

    @NotNull(message = "테마 아이디는 반드시 입력되어야 합니다.")
    @Positive(message = "테마 아이디는 자연수여야 합니다. ${validatedValue}은 사용할 수 없습니다.")
    private final Long themeId;

    @DateFormatConstraint
    private final String date;

    @NotNull(message = "예약 시간 아이디는 반드시 입력되어야 합니다.")
    @Positive(message = "예약 시간 아이디는 자연수여야 합니다. ${validatedValue}은 사용할 수 없습니다.")
    private final Long timeId;

    public MemberReservationRequest(Long themeId, String date, Long timeId) {
        this.themeId = themeId;
        this.date = date;
        this.timeId = timeId;
    }

    public Long getThemeId() {
        return themeId;
    }

    public String getDate() {
        return date;
    }

    public long getTimeId() {
        return timeId;
    }
}
