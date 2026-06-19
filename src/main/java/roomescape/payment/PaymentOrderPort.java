package roomescape.payment;

import java.util.Optional;

/**
 * 예약이 결제에 협력하기 위한 유일한 진입점(포트). 예약은 이 인터페이스와 OrderTicket 만 알고,
 * 결제 내부(Order, OrderRepository, Toss)는 모른다.
 */
public interface PaymentOrderPort {

    OrderTicket placeOrder(long reservationId);

    Optional<OrderTicket> findTicket(long reservationId);

}
