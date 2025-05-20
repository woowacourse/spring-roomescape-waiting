package roomescape.reservation.presentation.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class ReservationRequest {

    @NotNull
    private final LocalDate date;

    @NotNull
    private final Long themeId;

    @NotNull
    private final Long timeId;

    public ReservationRequest(final LocalDate date, final Long themeId, final Long timeId) {
        this.date = date;
        this.themeId = themeId;
        this.timeId = timeId;
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
