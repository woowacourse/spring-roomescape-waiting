package roomescape.dto.request;

import roomescape.dto.InputValidator;

import java.time.LocalDate;

public record MemberReservationRequest(LocalDate date, Long timeId, Long themeId) {

    public MemberReservationRequest {
        InputValidator.validateNotNull(date, timeId, themeId);
    }
}
