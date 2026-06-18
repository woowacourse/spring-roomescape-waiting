package roomescape.payment.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.order.Order;
import roomescape.order.OrderService;
import roomescape.order.OrderStatus;
import roomescape.payment.PaymentGateway;
import roomescape.reservation.service.ReservationService;

/**
 * 결제는 됐지만 예약 확정에 실패한(NEEDS_REFUND) 주문을 환불한다 — 보상 트랜잭션(Saga).
 * 게이트웨이 취소를 호출(멱등키로 재시도 안전)해, 성공하면 FAILED로 수렴시키고 예약을 정리한다.
 * 취소가 불명확/실패면 예외가 전파돼 상태가 NEEDS_REFUND로 남고, 워커가 다음 주기에 재시도한다.
 * reconciliation과 같은 폴링 패턴(아웃박스/최종 일관성)을 '환불(반대 행동)' 방향에 한 겹 더 적용한 것.
 */
@Service
@Transactional
public class PaymentRefundService {

    private final PaymentGateway paymentGateway;
    private final OrderService orderService;
    private final ReservationService reservationService;

    public PaymentRefundService(PaymentGateway paymentGateway, OrderService orderService,
                                ReservationService reservationService) {
        this.paymentGateway = paymentGateway;
        this.orderService = orderService;
        this.reservationService = reservationService;
    }

    @Transactional(readOnly = true)
    public List<String> findRefundableOrderIds() {
        return orderService.findNeedsRefundOrderIds();
    }

    /**
     * 환불(보상)이 필요한 주문 한 건을 처리한다. 멱등키로 취소를 호출하고, 성공하면 FAILED + 예약 정리로 수렴한다.
     * 취소가 불명확/실패면 예외를 그대로 던져(상태 NEEDS_REFUND 유지) 다음 주기에 재시도한다(멱등이라 안전).
     */
    public void refund(String orderId) {
        Order order = orderService.findByOrderId(orderId).orElse(null);
        if (order == null || order.getStatus() != OrderStatus.NEEDS_REFUND) {
            return; // 이미 환불·수렴됐으면 건너뛴다(멱등).
        }
        // 주문당 고정 멱등키를 그대로 보낸다. 토스는 멱등키를 엔드포인트별로 구분하므로 confirm에 쓴 같은 키라도
        // cancel에선 별개로 취급된다 — 이중 환불 방지(불명확→재시도 안전)는 토스의 멱등 처리에 기댄다.
        paymentGateway.cancel(order.getPaymentKey(), order.getIdempotencyKey());
        // CAS로 NEEDS_REFUND→FAILED를 선점한 쪽만 수렴시킨다. 예약은 보통 이미 CANCELED(그래서 확정이 실패했다)라
        // cancelPending은 멱등 가드로 무동작 — 슬롯은 예약 취소 시점에 이미 풀렸다(여기선 안전한 방어 호출).
        if (orderService.markFailed(order)) {
            reservationService.cancelPending(order.getReservationId());
        }
    }
}
