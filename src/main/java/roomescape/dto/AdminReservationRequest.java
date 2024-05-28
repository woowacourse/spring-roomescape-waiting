package roomescape.dto;

import java.time.LocalDate;

public record AdminReservationRequest(
        Long memberId,
        LocalDate date,
        Long timeId,
        Long themeId
) implements ReservationRequest {

    public AdminReservationRequest {
        InputValidator.validateNotNull(memberId, date, timeId, themeId);
    }
}
