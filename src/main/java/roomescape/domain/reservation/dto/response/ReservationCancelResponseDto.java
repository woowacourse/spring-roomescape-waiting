package roomescape.domain.reservation.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ReservationCancelResponseDto(Long id, String name, LocalDate date, Long timeId, Long themeId,
                                           LocalDateTime canceledAt) {

}
