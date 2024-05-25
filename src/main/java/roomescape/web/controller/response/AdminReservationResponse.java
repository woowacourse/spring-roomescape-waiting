package roomescape.web.controller.response;

import java.time.LocalDate;

public record AdminReservationResponse(LocalDate date, Long themeId, Long timeId, Long memberId) {
}
