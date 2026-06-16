package roomescape.reservation.controller.dto.response;

import roomescape.payment.domain.Payment;
import roomescape.payment.domain.PaymentStatus;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.repository.dto.ReservationWithSlotInformation;
import roomescape.slot.domain.ReservationSlot;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservationWithSlotDetailDto(
        Long id,
        Long slotId,
        String name,
        LocalDate date,
        LocalTime time,
        Long themeId,
        String themeName,
        String themeThumbnailUrl,
        ReservationStatus status,
        Long waitingTurn,
        String orderId,
        Long amount,
        PaymentStatus paymentStatus
) {

    public static ReservationWithSlotDetailDto from(ReservationWithSlotInformation projection) {
        return new ReservationWithSlotDetailDto(
                projection.id(),
                projection.slotId(),
                projection.name(),
                projection.date(),
                projection.time(),
                projection.themeId(),
                projection.themeName(),
                projection.themeThumbnailUrl(),
                projection.status(),
                projection.waitingTurn(),
                projection.orderId(),
                projection.paymentAmount(),
                projection.paymentStatus()
        );
    }

    public static ReservationWithSlotDetailDto of(Reservation reservation, ReservationSlot slot, Payment payment) {
        return new ReservationWithSlotDetailDto(
                reservation.getId(),
                reservation.getSlotId(),
                reservation.getName(),
                slot.getDate().getDate(),
                slot.getTime().getStartAt(),
                slot.getThemeId(),
                slot.getTheme().getName(),
                slot.getTheme().getThumbnailUrl(),
                reservation.getStatus(),
                null,
                payment.getOrderId(),
                payment.getAmount(),
                payment.getStatus()
        );
    }

    public static ReservationWithSlotDetailDto of(Reservation reservation, ReservationSlot slot) {
        return new ReservationWithSlotDetailDto(
                reservation.getId(),
                reservation.getSlotId(),
                reservation.getName(),
                slot.getDate().getDate(),
                slot.getTime().getStartAt(),
                slot.getThemeId(),
                slot.getTheme().getName(),
                slot.getTheme().getThumbnailUrl(),
                reservation.getStatus(),
                null,
                null,
                null,
                null
        );
    }
}
