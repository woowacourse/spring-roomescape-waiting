package roomescape.web.controller.response;

import java.time.LocalDate;

public record AdminReservationWebResponse(LocalDate date, Long themeId, Long timeId, Long memberId) {
}
