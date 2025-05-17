package roomescape.admin.domain.dto;

import java.time.LocalDate;

public record AdminReservationRequestDto(LocalDate date, Long themeId, Long timeId, Long memberId) {
}
