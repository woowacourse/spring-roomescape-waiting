package roomescape.service.dto.input;

import java.time.LocalDate;

public record ReservationSearchInput(long themeId, long memberId, LocalDate fromDate, LocalDate toDate) {
}
