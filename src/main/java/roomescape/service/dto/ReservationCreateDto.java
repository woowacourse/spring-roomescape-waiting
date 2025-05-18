package roomescape.service.dto;

import java.time.LocalDate;

public record ReservationCreateDto(LocalDate date, long timeId, long themeId, long memberId) {
}
