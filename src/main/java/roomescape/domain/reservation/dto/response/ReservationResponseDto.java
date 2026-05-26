package roomescape.domain.reservation.dto.response;

import java.time.LocalDate;
import roomescape.domain.theme.dto.response.ThemeResponseDto;
import roomescape.domain.time.dto.response.TimeResponseDto;

public record ReservationResponseDto(Long id, String name, LocalDate date, TimeResponseDto time,
                                     ThemeResponseDto theme, boolean canceled) {

}
