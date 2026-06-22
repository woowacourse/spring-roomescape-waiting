package roomescape.controller.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public class ReservationUpdateRequest {
    @NotNull(message = "회원 ID는 필수로 입력해야 합니다.")
    @Positive(message = "회원 ID는 양수여야 합니다.")
    private final Long memberId;

    @NotNull(message = "날짜는 필수로 입력해야 합니다")
    private final LocalDate date;

    @NotNull(message = "Time ID는 필수로 입력해야 합니다.")
    @Positive(message = "Time ID는 양수여야 합니다.")
    private final Long timeId;

    @NotNull(message = "Theme ID는 필수로 입력해야 합니다.")
    @Positive(message = "Theme ID는 양수여야 합니다.")
    private final Long themeId;

    public ReservationUpdateRequest(Long memberId, LocalDate date, Long timeId, Long themeId) {
        this.memberId = memberId;
        this.date = date;
        this.timeId = timeId;
        this.themeId = themeId;
    }

    public Long getMemberId() {
        return memberId;
    }

    public LocalDate getDate() {
        return date;
    }

    public Long getTimeId() {
        return timeId;
    }

    public Long getThemeId() {
        return themeId;
    }
}
