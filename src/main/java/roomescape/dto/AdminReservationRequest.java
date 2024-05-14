package roomescape.dto;

import java.time.LocalDate;

public record AdminReservationRequest(LocalDate date, long timeId, long themeId, long memberId) {
}
