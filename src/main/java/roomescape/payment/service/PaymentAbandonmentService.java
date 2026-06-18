package roomescape.payment.service;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.service.ReservationAuthorizationService;
import roomescape.member.Member;
import roomescape.order.Order;
import roomescape.order.OrderService;
import roomescape.reservation.service.ReservationService;

/**
 * 결제 없이 방치된(abandonment) 주문 정리 책임. 활성 결제 흐름(PaymentService)과 분리한다.
 * failUrl 콜백과 만료 스케줄러(reaper)가 트리거만 다르고, 정리 로직(abandon)은 하나로 공유한다.
 */
@Service
@Transactional
public class PaymentAbandonmentService {

    private final OrderService orderService;
    private final ReservationService reservationService;
    private final ReservationAuthorizationService authorizationService;

    public PaymentAbandonmentService(OrderService orderService, ReservationService reservationService,
                                     ReservationAuthorizationService authorizationService) {
        this.orderService = orderService;
        this.reservationService = reservationService;
        this.authorizationService = authorizationService;
    }

    /**
     * failUrl 처리. 사용자가 취소(PAY_PROCESS_CANCELED)하면 orderId가 없을 수 있어 null 가드를 둔다.
     */
    public void fail(Member member, String orderId) {
        if (orderId == null || orderId.isBlank()) {
            return;
        }
        orderService.findByOrderId(orderId).ifPresent(order -> {
            authorizationService.validateMemberCanAccess(member, order.getReservationId());
            abandon(order);
        });
    }

    /**
     * 결제 신호 없이 방치된 주문 후보를 찾는다(주문 기준). 갓 만든 PENDING은 기준 시각보다 어려서 안 걸린다.
     */
    @Transactional(readOnly = true)
    public List<String> findExpiredPendingOrderIds(LocalDateTime threshold) {
        return orderService.findExpiredPendingOrderIds(threshold);
    }

    /**
     * 만료된 주문 한 건을 정리한다. failUrl 콜백과 똑같이 abandon을 재사용한다(트리거만 다르고 정리 로직은 하나).
     */
    public void expire(String orderId) {
        orderService.findByOrderId(orderId).ifPresent(this::abandon);
    }

    /**
     * 방치된 주문을 실패 처리하고, 예약 취소(슬롯 해제 + 다음 대기자 승격)는 예약 서비스에 위임한다.
     * 이미 확정/정리됐으면 건너뛴다(멱등).
     */
    private void abandon(Order order) {
        if (!order.isPending()) {
            return;
        }
        orderService.markFailed(order);
        reservationService.cancelPending(order.getReservationId());
    }
}
