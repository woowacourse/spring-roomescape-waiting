package roomescape.service.dto;

import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

import java.time.LocalDate;

public record UserReservation(
        Long id,
        String name,
        LocalDate date,
        ReservationTime time,
        Theme theme,
        ReservationStatus status,
        Long rank,
        String orderId,
        String paymentKey,
        Long amount
) {
}
