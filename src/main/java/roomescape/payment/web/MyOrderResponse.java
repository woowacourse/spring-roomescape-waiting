package roomescape.payment.web;

import roomescape.order.Order;

/**
 * 내 결제/주문 내역 한 건. 예약과 reservationId로 묶어 프론트가 예약 정보와 합쳐 보여준다.
 * status는 주문 상태(PENDING/CONFIRMED/FAILED/NEEDS_CHECK)로, NEEDS_CHECK는 '확인 필요'로 표시된다.
 */
public record MyOrderResponse(Long reservationId, String orderId, String status, String paymentKey, Long amount) {
    public static MyOrderResponse from(Order order) {
        return new MyOrderResponse(order.getReservationId(), order.getOrderId(),
                order.getStatus().name(), order.getPaymentKey(), order.getAmount());
    }
}
