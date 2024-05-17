package roomescape.dto.request;

import java.time.LocalDate;

import static roomescape.dto.InputValidator.validateNotNull;

public record AdminReservationRequest(Long memberId, LocalDate date, Long timeId, Long themeId) {

    public AdminReservationRequest {
        validateNotNull(memberId, date, timeId, themeId);
    }
}
