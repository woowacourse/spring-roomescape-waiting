package roomescape.service.dto;

import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

import java.time.LocalDate;
import roomescape.domain.ReservationStatus;

public record UserReservation(
        Long id,
        String name,
        LocalDate date,
        ReservationTime time,
        Theme theme,
        ReservationStatus status,
        Long rank
) {
}
