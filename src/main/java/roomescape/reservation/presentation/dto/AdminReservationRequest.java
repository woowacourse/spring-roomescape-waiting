package roomescape.reservation.presentation.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class AdminReservationRequest {

    @NotNull(message = "날짜는 반드시 입력해야 합니다")
    private final LocalDate date;

    @NotNull(message = "테마는 반드시 입력해야 합니다")
    private final Long themeId;

    @NotNull(message = "시작 시간은 반드시 입력해야 합니다")
    private final Long timeId;

    @NotNull(message = "유저는 반드시 입력해야 합니다")
    private final Long memberId;

    public AdminReservationRequest(LocalDate date, Long themeId, Long timeId, Long memberId) {
        this.date = date;
        this.themeId = themeId;
        this.timeId = timeId;
        this.memberId = memberId;
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

    public Long getMemberId() {
        return memberId;
    }
}
