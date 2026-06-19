package roomescape.payment;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.service.ReservationAuthorizationService;
import roomescape.common.exception.EntityNotFoundException;
import roomescape.member.Member;
import roomescape.order.Order;
import roomescape.order.OrderService;
import roomescape.payment.web.PaymentReadyResponse;
import roomescape.reservation.Reservation;
import roomescape.reservation.service.ReservationService;

/**
 * 결제 애플리케이션 서비스. 토스를 모르고 PaymentGateway(포트)만 의존한다.
 * 결제 흐름을 *조율*만 한다 — 주문(OrderService)·예약(ReservationService) 상태는 각 서비스가 소유하고,
 * 여기서는 게이트웨이 호출과 그 결과 전파(주문 확정/실패 → 예약 확정/취소)만 맡는다.
 */
@Service
@Transactional
public class PaymentService {
    private final PaymentGateway paymentGateway;
    private final OrderService orderService;
    private final ReservationService reservationService;
    private final ReservationAuthorizationService authorizationService;

    public PaymentService(PaymentGateway paymentGateway, OrderService orderService,
                          ReservationService reservationService,
                          ReservationAuthorizationService authorizationService) {
        this.paymentGateway = paymentGateway;
        this.orderService = orderService;
        this.reservationService = reservationService;
        this.authorizationService = authorizationService;
    }

    /**
     * 프론트 결제창에 쓰는 공개 클라이언트 키. 포트를 통해 노출해 컨트롤러가 토스를 모르게 한다.
     */
    public String clientKey() {
        return paymentGateway.clientKey();
    }

    /**
     * 결제 시작. 결제 대기(PENDING) 예약에 묶을 주문을 이 시점에 만들고(= 주문 = 결제 의사), 결제창 구동에
     * 필요한 정보를 돌려준다. 금액은 클라이언트를 믿지 않고 서버가 테마 가격으로 정한다.
     */
    public PaymentReadyResponse prepare(Member member, Long reservationId) {
        authorizationService.validateMemberCanAccess(member, reservationId);
        Reservation reservation = reservationService.findById(reservationId);
        Order order = orderService.getOrCreate(reservationId, reservation.getTheme().getPrice());
        return PaymentReadyResponse.from(reservation, order);
    }

    /**
     * successUrl 콜백 처리. 저장된 주문 금액과 대조한 뒤(검증), 통과해야 승인 API를 호출한다.
     * 승인되면 주문을 확정하고 예약 확정(BOOKED)을 예약 서비스에 위임한다.
     */
    public void confirm(Member member, String paymentKey, String orderId, long amount) {
        Order order = orderService.findByOrderId(orderId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 주문입니다."));
        authorizationService.validateMemberCanAccess(member, order.getReservationId());
        order.validateAmount(amount);

        PaymentResult result = paymentGateway.confirm(new PaymentConfirmation(paymentKey, orderId, amount));

        orderService.complete(order, result.paymentKey());
        reservationService.confirm(order.getReservationId());
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
