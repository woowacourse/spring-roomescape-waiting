package payment;

/**
 * 결제가 예약 쪽에 노출하는 주문 식별 정보. 결제 내부의 Order 엔티티 대신 이 record 만 경계를 넘는다.
 */
public record OrderTicket(String orderId, Long amount) {

}
