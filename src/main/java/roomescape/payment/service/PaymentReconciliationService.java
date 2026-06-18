package roomescape.payment.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.order.Order;
import roomescape.order.OrderService;
import roomescape.order.OrderStatus;
import roomescape.payment.PaymentApprovalStatus;
import roomescape.payment.PaymentGateway;
import roomescape.reservation.service.ReservationService;

/**
 * 결과 불명확(NEEDS_CHECK) 주문을 게이트웨이에 조회해 확정/실패로 수렴시킨다(reconciliation).
 * 사용자가 재시도(recheck)하지 않아도 림보에 갇히지 않게, 워커가 주기적으로 이 서비스를 호출한다.
 * 게이트웨이를 진실의 원천 삼아 — 승인됐으면 (기존 확정 경로), 아니면 (기존 정리 경로)로 수렴한다.
 */
@Service
@Transactional
public class PaymentReconciliationService {

    private final PaymentGateway paymentGateway;
    private final OrderService orderService;
    private final ReservationService reservationService;

    public PaymentReconciliationService(PaymentGateway paymentGateway, OrderService orderService,
                                        ReservationService reservationService) {
        this.paymentGateway = paymentGateway;
        this.orderService = orderService;
        this.reservationService = reservationService;
    }

    @Transactional(readOnly = true)
    public List<String> findReconcilableOrderIds() {
        return orderService.findNeedsCheckOrderIds();
    }

    public void reconcile(String orderId) {
        Order order = orderService.findByOrderId(orderId).orElse(null);
        if (order == null || order.getStatus() != OrderStatus.NEEDS_CHECK) {
            return; // 사용자 recheck 등으로 이미 수렴됐으면 건너뛴다(멱등).
        }
        PaymentApprovalStatus status = paymentGateway.findStatus(orderId);
        // 상태 전이를 CAS로 선점한 쪽만 예약 후속을 진행한다 — 워커와 사용자 recheck가 동시에 와도(또는
        // 여러 인스턴스의 워커가 동시에 와도) DB가 한쪽만 이기게 직렬화한다(낙관, status가 곧 version).
        if (status == PaymentApprovalStatus.APPROVED) {
            if (orderService.complete(order, order.getPaymentKey())) {
                reservationService.confirm(order.getReservationId());
            }
        } else {
            if (orderService.markFailed(order)) {
                reservationService.cancelPending(order.getReservationId());
            }
        }
    }
}
