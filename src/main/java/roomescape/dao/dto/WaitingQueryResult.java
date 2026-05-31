package roomescape.dao.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.domain.reservation.UserName;
import roomescape.domain.reservation.theme.Theme;
import roomescape.domain.reservation.time.ReservationTime;

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
