package roomescape.reservation.presentation.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class AdminReservationRequest {
    @NotNull
    private final LocalDate date;

    @NotNull
    private final Long themeId;

    @NotNull
    private final Long timeId;

    @NotNull
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
