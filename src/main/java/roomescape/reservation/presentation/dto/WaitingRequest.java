package roomescape.reservation.presentation.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class WaitingRequest {

    @NotNull(message = "날짜는 반드시 입력해야 합니다")
    private final LocalDate date;

    @NotNull(message = "테마는 반드시 입력해야 합니다")
    private final Long themeId;

    @NotNull(message = "시작 시간은 반드시 입력해야 합니다")
    private final Long timeId;

    public WaitingRequest(final LocalDate date, final Long themeId, final Long timeId) {
        this.date = date;
        this.themeId = themeId;
        this.timeId = timeId;
    }

    public LocalDate getDate() {
        return date;
    }

    public Long getThemeId() {
        return themeId;
    }

    public Long getTimeId() {
        return timeId;
    }
}
