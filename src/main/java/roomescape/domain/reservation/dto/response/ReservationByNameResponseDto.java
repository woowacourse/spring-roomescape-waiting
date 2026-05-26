package roomescape.domain.reservation.dto.response;

import java.time.LocalDate;
import roomescape.domain.theme.dto.response.ReservationThemeResponseDto;
import roomescape.domain.time.dto.response.ReservationTimeResponseDto;

public record ReservationByNameResponseDto(Long id, String name, LocalDate date, ReservationTimeResponseDto time,
                                           ReservationThemeResponseDto theme, ReservationStatus status,
                                           String message) {

}
