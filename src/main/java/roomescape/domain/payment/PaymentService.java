package roomescape.domain.payment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.service.ReservationAuthorizationService;
import roomescape.common.exception.EntityNotFoundException;
import roomescape.dao.OrderDao;
import roomescape.reservation.ReservationDao;
import roomescape.domain.member.Member;
import roomescape.reservation.Reservation;

/**
 * 결제 애플리케이션 서비스. 토스를 모르고 PaymentGateway(포트)만 의존한다.
 * 주문 저장(결제 전), 승인(검증→게이트웨이→확정), 실패 정리를 오케스트레이션한다.
 */
@Service
@Transactional
public class PaymentService {
    private final OrderDao orderDao;
    private final PaymentGateway paymentGateway;
    private final ReservationDao reservationDao;
    private final ReservationAuthorizationService authorizationService;

    public PaymentService(OrderDao orderDao, PaymentGateway paymentGateway, ReservationDao reservationDao,
                          ReservationAuthorizationService authorizationService) {
        this.orderDao = orderDao;
        this.paymentGateway = paymentGateway;
        this.reservationDao = reservationDao;
        this.authorizationService = authorizationService;
    }

    /**
     * 결제 인증 전, 주문 정보(orderId·금액)를 먼저 저장한다. orderId는 서버가 UUID로 생성한다.
     */
    public Order createOrder(Long reservationId, long amount) {
        String orderId = UUID.randomUUID().toString();
        return orderDao.insert(Order.create(orderId, reservationId, amount));
    }

    /**
     * 프론트 결제창에 쓰는 공개 클라이언트 키. 포트를 통해 노출해 컨트롤러가 토스를 모르게 한다.
     */
    public String clientKey() {
        return paymentGateway.clientKey();
    }

    /**
     * successUrl 콜백 처리. 저장된 주문 금액과 대조한 뒤(검증), 통과해야 승인 API를 호출한다.
     * 조작된 금액은 validateAmount에서 막혀 게이트웨이까지 가지 않는다.
     */
    public void confirm(Member member, String paymentKey, String orderId, long amount) {
        Order order = orderDao.findByOrderId(orderId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 주문입니다."));
        authorizationService.validateMemberCanAccess(member, order.getReservationId());
        order.validateAmount(amount);

        PaymentResult result = paymentGateway.confirm(new PaymentConfirmation(paymentKey, orderId, amount));

        order.complete(result.paymentKey());
        orderDao.update(order);
        confirmReservation(order.getReservationId());
    }

    /**
     * failUrl 처리. 사용자가 취소(PAY_PROCESS_CANCELED)하면 orderId가 없을 수 있어 null 가드를 둔다.
     * 결제 대기 상태의 주문/예약을 정리한다.
     */
    public void fail(Member member, String orderId) {
        if (orderId == null || orderId.isBlank()) {
            return;
        }
        orderDao.findByOrderId(orderId).ifPresent(order -> {
            authorizationService.validateMemberCanAccess(member, order.getReservationId());
            abandon(order);
        });
    }

    /**
     * 결제 신호 없이 방치된(abandonment) 주문 후보를 찾는다. 기준 시각보다 *이전에* 생성된 PENDING만 —
     * 갓 만든 PENDING(아직 결제 진행 중일 수 있음)은 기준 시각보다 어려서 걸리지 않는다.
     */
    @Transactional(readOnly = true)
    public List<String> findExpiredPendingOrderIds(LocalDateTime threshold) {
        return orderDao.findExpiredPending(threshold).stream()
                .map(Order::getOrderId)
                .toList();
    }

    /**
     * 만료된 주문 한 건을 정리한다. failUrl 콜백과 똑같이 abandon을 재사용한다(트리거만 다르고 정리 로직은 하나).
     * 다시 조회해 그 사이 확정/정리됐으면 abandon이 알아서 건너뛴다.
     */
    public void expire(String orderId) {
        orderDao.findByOrderId(orderId).ifPresent(this::abandon);
    }

    private void confirmReservation(Long reservationId) {
        Reservation reservation = reservationDao.findById(reservationId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 예약입니다."));
        reservation.confirm(LocalDateTime.now());
        reservationDao.update(reservation);
    }

    private void abandon(Order order) {
        if (!order.isPending()) {
            return;
        }
        order.markFailed();
        orderDao.update(order);
        reservationDao.findById(order.getReservationId()).ifPresent(reservation -> {
            reservation.cancelPending(LocalDateTime.now());
            reservationDao.update(reservation);
        });
    }
}
