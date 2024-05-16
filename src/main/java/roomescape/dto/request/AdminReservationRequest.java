package roomescape.dto.request;

import roomescape.dto.InputValidator;

import java.time.LocalDate;

public record AdminReservationRequest(Long memberId, LocalDate date, Long timeId, Long themeId) {

    public AdminReservationRequest {
        InputValidator.validateNotNull(memberId, date, timeId, themeId);
    }
}
