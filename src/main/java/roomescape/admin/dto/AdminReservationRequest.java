package roomescape.admin.dto;

import java.time.LocalDate;

public record AdminReservationRequest(LocalDate date, long themeId, long timeId, long memberId) {
}
