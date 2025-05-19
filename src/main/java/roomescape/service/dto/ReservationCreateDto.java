package roomescape.service.dto;

import java.time.LocalDate;

public record ReservationCreateDto(LocalDate date, long themeId, long timeId, long memberId) {
}
