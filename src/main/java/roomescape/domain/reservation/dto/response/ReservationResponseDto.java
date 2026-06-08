package roomescape.domain.reservation.dto.response;

import java.time.LocalDate;
import roomescape.domain.reservation.entity.ReservationEditableStatus;
import roomescape.domain.theme.dto.response.ReservationThemeResponseDto;
import roomescape.domain.time.dto.response.ReservationTimeResponseDto;

public record ReservationResponseDto(Long id, String name, LocalDate date, ReservationTimeResponseDto time,
                                     ReservationThemeResponseDto theme, ReservationEditableStatus status,
                                     String message, Integer waitingNumber, Long version) {

}
