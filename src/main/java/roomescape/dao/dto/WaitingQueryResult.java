package roomescape.dao.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.domain.reservation.UserName;
import roomescape.domain.slot.theme.Theme;
import roomescape.domain.slot.time.ReservationTime;

public record WaitingQueryResult(
        Long id,
        UserName name,
        LocalDate date,
        ReservationTime time,
        Theme theme,
        LocalDateTime createdAt,
        int sequence
) {
}
