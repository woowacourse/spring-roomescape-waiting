package roomescape.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import roomescape.domain.PaymentOrder;
import roomescape.domain.Reservation;
import roomescape.domain.Theme;
import roomescape.domain.Time;
import roomescape.domain.vo.ReservationSlotInfo;

public record ReservationResponse(
        long reservationId,
        String name,
        String status,
        LocalDate date,
        String themeName,
        String themeDescription,
        String themeThumbnailUrl,
        @JsonFormat(pattern = "HH:mm")
        LocalTime time,
        int order,
        String orderId,
        String paymentStatus,
        String paymentKey,
        Long amount) {
    public static ReservationResponse from(Reservation reservation, ReservationSlotInfo reservationSlotInfo, int order) {
        return from(reservation, reservationSlotInfo, order, null);
    }

    public static ReservationResponse from(Reservation reservation, ReservationSlotInfo reservationSlotInfo, int order, PaymentOrder paymentOrder) {
        Theme theme = reservationSlotInfo.theme();
        Time time = reservationSlotInfo.time();

        return new ReservationResponse(
                reservation.getId(),
                reservation.getName(),
                reservation.getStatus().toString(),
                reservationSlotInfo.date(),
                theme.getName(),
                theme.getDescription(),
                theme.getThumbnailUrl(),
                time.getStartAt(),
                order,
                paymentOrder == null ? null : paymentOrder.getOrderId(),
                paymentOrder == null ? null : paymentOrder.getStatus().name(),
                null,
                paymentOrder == null ? null : paymentOrder.getAmount()
        );
    }
}
