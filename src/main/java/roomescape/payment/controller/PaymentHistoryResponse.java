package roomescape.payment.controller;

import roomescape.payment.order.PaymentOrderStatus;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 주문/결제 내역 한 줄. 예약 정보(테마·날짜·시간)와 결제 상태(대기/확정/실패/확인 필요),
 * orderId·paymentKey·금액을 함께 담는다.
 */
public record PaymentHistoryResponse(
        Long reservationId,
        String themeName,
        LocalDate date,
        LocalTime time,
        String status,
        String statusLabel,
        String orderId,
        String paymentKey,
        Long amount,
        Long approvedAmount
) {
    public static PaymentHistoryResponse of(
            Long reservationId, String themeName, LocalDate date, LocalTime time,
            PaymentOrderStatus status, String orderId, String paymentKey, Long amount, Long approvedAmount
    ) {
        return new PaymentHistoryResponse(
                reservationId, themeName, date, time,
                status.name(), status.getLabel(), orderId, paymentKey, amount, approvedAmount
        );
    }
}
