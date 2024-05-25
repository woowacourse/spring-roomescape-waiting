package roomescape.dto.request;

import java.time.LocalDate;

import roomescape.dto.InputValidator;

public record MemberReservationRequest(LocalDate date, Long timeId, Long themeId) implements ReservationRequest {

    public MemberReservationRequest {
        InputValidator.validateNotNull(date, timeId, themeId);
    }
}
