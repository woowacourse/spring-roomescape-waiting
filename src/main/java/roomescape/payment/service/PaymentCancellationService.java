package roomescape.payment.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.member.Member;
import roomescape.order.OrderService;
import roomescape.reservation.service.ReservationService;

/**
 * 결제된 예약의 정상 취소를 조율한다. 예약 취소(슬롯 해제·승격)와 환불 예약을 한 트랜잭션으로 묶는다 —
 * order·reservation을 둘 다 아는 payment 계층이 조율해야 ReservationService가 payment를 모른 채 남는다
 * (reservation↔payment 사이클 회피).
 *
 * 환불은 여기서 게이트웨이를 직접 호출하지 않는다. 외부 호출보다 *먼저* 주문을 NEEDS_REFUND로 커밋해 두면
 * (intent-first / 아웃박스), 기존 PaymentRefundWorker가 그 주문을 주워 게이트웨이 취소로 환불한다.
 * 결제 없이 확정된(어드민) 예약은 환불 대상 주문이 없어 취소만 일어난다.
 */
@Service
@Transactional
public class PaymentCancellationService {

    private final ReservationService reservationService;
    private final OrderService orderService;

    public PaymentCancellationService(ReservationService reservationService, OrderService orderService) {
        this.reservationService = reservationService;
        this.orderService = orderService;
    }

    public void cancelBooking(Member member, Long reservationId) {
        // 권한 검증·슬롯 해제·다음 대기자 승격은 예약 서비스가 소유한다(payment를 모른다).
        reservationService.cancel(reservationId, member);
        // 결제된 주문이 있으면 환불 대기로 표시 — 실제 환불(외부 호출)은 워커가 한다. 외부 호출보다 먼저
        // 커밋되므로(이 트랜잭션 종료 시), 워커가 주울 흔적이 항상 남는다.
        orderService.findConfirmedByReservationId(reservationId)
                .ifPresent(orderService::markNeedsRefund);
    }
}
