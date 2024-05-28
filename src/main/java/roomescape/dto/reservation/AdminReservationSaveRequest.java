package roomescape.dto.reservation;

import java.time.LocalDate;

public record AdminReservationSaveRequest(
        Long memberId,
        LocalDate date,
        Long timeId,
        Long themeId
) {
}
