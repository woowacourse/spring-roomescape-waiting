package roomescape.feature.reservation.dto.response;

import java.time.LocalDate;
import roomescape.feature.reservation.domain.OrderStatus;
import roomescape.feature.theme.dto.response.ReservationThemeResponseDto;
import roomescape.feature.time.dto.response.ReservationTimeResponseDto;

public record ReservationResponseDto(Long id, String name, LocalDate date, ReservationTimeResponseDto time,
                                     ReservationThemeResponseDto theme, ReservationEditableStatus status,
                                     String message, Integer waitingNumber, OrderStatus orderStatus) {

}
