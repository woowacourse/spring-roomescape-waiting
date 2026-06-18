package roomescape.payment.domain;

import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PaymentOrderDetails(
        String orderId,
        long amount,
        PaymentOrderStatus status,
        String name,
        LocalDate date,
        ReservationTime time,
        Theme theme,
        Long reservationId,
        String failureCode,
        String failureMessage,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime confirmedAt
) {
}
