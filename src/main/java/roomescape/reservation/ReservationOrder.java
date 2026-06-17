package roomescape.reservation;

import roomescape.order.Order;

/**
 * 예약 생성(결제 전) 결과. 결제 대기 예약과 그에 묶인 주문을 함께 전달한다.
 */
public record ReservationOrder(Reservation reservation, Order order) {
}
