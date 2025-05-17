package roomescape.admin.domain.dto;

import java.time.LocalDate;

public record SearchReservationRequestDto(Long userId, Long themeId, LocalDate from, LocalDate to) {
}
