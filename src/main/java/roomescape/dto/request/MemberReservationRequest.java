package roomescape.dto.request;

import java.time.LocalDate;

import static roomescape.dto.request.exception.InputValidator.validateNotNull;

public record MemberReservationRequest(LocalDate date, Long timeId, Long themeId) {

    public MemberReservationRequest {
        validateNotNull(date, timeId, themeId);
    }
}
