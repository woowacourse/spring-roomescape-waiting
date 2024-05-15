package roomescape.dto.request;

import java.time.LocalDate;

import roomescape.dto.InputValidator;

public record AdminReservationRequest(Long memberId, LocalDate date, Long timeId, Long themeId) {

    public AdminReservationRequest {
        InputValidator.validateNotNull(memberId, date, timeId, themeId);
    }
}
