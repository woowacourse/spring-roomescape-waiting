package roomescape.domain.reservation.dto.response;

import java.time.LocalDate;

public record ReservationCancelResponseDto(Long id, String name, LocalDate date, Long timeId, Long themeId) {

}
