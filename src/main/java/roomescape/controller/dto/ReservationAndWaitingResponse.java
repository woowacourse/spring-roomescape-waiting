package roomescape.controller.dto;

import java.time.LocalDate;
import roomescape.domain.payment.OrderStatus;
import roomescape.domain.reservation.ReservationAndWaiting;
import roomescape.domain.reservation.ReservationPaymentInfo;

public record ReservationAndWaitingResponse(
        long id,
        String name,
        LocalDate date,
        TimeResponse time,
        ThemeResponse theme,
        boolean isReserved,
        Integer waitingNumber,
        OrderStatus paymentStatus,
        String orderId,
        String paymentKey,
        Long amount
) {

    public static ReservationAndWaitingResponse from(ReservationAndWaiting reservationAndWaiting) {
        ReservationPaymentInfo paymentInfo = reservationAndWaiting.paymentInfo();
        return new ReservationAndWaitingResponse(
                reservationAndWaiting.id(),
                reservationAndWaiting.name(),
                reservationAndWaiting.date(),
                TimeResponse.from(reservationAndWaiting.timeSlot()),
                ThemeResponse.from(reservationAndWaiting.theme()),
                reservationAndWaiting.isReserved(),
                toWaitingNumber(reservationAndWaiting.waitingIndex()),
                paymentStatus(paymentInfo),
                orderId(paymentInfo),
                paymentKey(paymentInfo),
                amount(paymentInfo)
        );
    }

    private static Integer toWaitingNumber(Integer waitingIndex) {
        if (waitingIndex == null) {
            return null;
        }
        return waitingIndex + 1;
    }

    private static OrderStatus paymentStatus(ReservationPaymentInfo paymentInfo) {
        if (paymentInfo == null) {
            return null;
        }
        return paymentInfo.status();
    }

    private static String orderId(ReservationPaymentInfo paymentInfo) {
        if (paymentInfo == null) {
            return null;
        }
        return paymentInfo.orderId();
    }

    private static String paymentKey(ReservationPaymentInfo paymentInfo) {
        if (paymentInfo == null) {
            return null;
        }
        return paymentInfo.paymentKey();
    }

    private static Long amount(ReservationPaymentInfo paymentInfo) {
        if (paymentInfo == null) {
            return null;
        }
        return paymentInfo.amount();
    }
}
