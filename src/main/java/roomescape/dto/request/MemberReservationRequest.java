package roomescape.dto.request;

import java.time.LocalDate;

import static roomescape.dto.request.exception.InputValidator.validateNotNull;

public record MemberReservationRequest(
        LocalDate date,
        Long timeId,
        Long themeId) implements ReservationCreationRequest {

    public MemberReservationRequest {
        validateNotNull(date, timeId, themeId);
    }

    @Override
    public LocalDate getDate() {
        return this.date;
    }

    @Override
    public Long getTimeId() {
        return this.timeId;
    }

    @Override
    public Long getThemeId() {
        return this.themeId;
    }
}
