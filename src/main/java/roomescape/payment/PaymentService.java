package roomescape.payment;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.service.ReservationAuthorizationService;
import roomescape.common.exception.BusinessRuleViolationException;
import roomescape.common.exception.EntityNotFoundException;
import roomescape.member.Member;
import roomescape.order.Order;
import roomescape.order.OrderService;
import roomescape.order.OrderStatus;
import roomescape.payment.exception.PaymentResultUnknownException;
import roomescape.payment.web.dto.PaymentReadyResponse;
import roomescape.reservation.Reservation;
import roomescape.reservation.service.ReservationService;

/**
 * 결제 애플리케이션 서비스. 토스를 모르고 PaymentGateway(포트)만 의존한다.
 * 결제 흐름을 *조율*만 한다 — 주문(OrderService)·예약(ReservationService) 상태는 각 서비스가 소유하고,
 * 여기서는 게이트웨이 호출과 그 결과 전파(주문 확정 → 예약 확정)만 맡는다. 방치 주문 정리는 OrderAbandonmentService로 분리했다.
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
     */
    public ConfirmOutcome confirm(Member member, String paymentKey, String orderId, long amount) {
        Order order = orderService.findByOrderId(orderId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 주문입니다."));
        authorizationService.validateMemberCanAccess(member, order.getReservationId());
        order.validateAmount(amount);
        return approve(order, paymentKey);
    }

    /**
     * 결과 불명확(NEEDS_CHECK)으로 남은 주문을 재확인한다. 저장된 paymentKey와 주문의 멱등키로 다시 승인 요청하면
     * 토스가 첫 응답을 그대로 돌려줘(멱등) 이중 승인 없이 결과를 확정한다(여전히 모름이면 NEEDS_CHECK 유지).
     */
    public ConfirmOutcome recheck(Member member, String orderId) {
        Order order = orderService.findByOrderId(orderId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 주문입니다."));
        authorizationService.validateMemberCanAccess(member, order.getReservationId());
        if (order.getStatus() != OrderStatus.NEEDS_CHECK) {
            throw new BusinessRuleViolationException("확인이 필요한 주문이 아닙니다.");
        }
        return approve(order, order.getPaymentKey());
    }

    /**
     * 게이트웨이 승인을 호출하고 결과를 주문/예약 상태로 전파한다(confirm·recheck 공유).
     * 승인되면 주문 확정(CONFIRMED)+예약 확정(BOOKED). read timeout처럼 결과가 불명확하면 실패로 단정하지 않고
     * 시도한 paymentKey와 함께 주문을 NEEDS_CHECK로 남긴다 — 예외를 다시 던지지 않아 @Transactional이 커밋한다.
     * connect 실패·토스 에러는 잡지 않아 그대로 전파된다(롤백 + advice 안내).
     */
    private ConfirmOutcome approve(Order order, String paymentKey) {
        PaymentResult result;
        try {
            result = paymentGateway.confirm(new PaymentConfirmation(
                    paymentKey, order.getOrderId(), order.getAmount(), order.getIdempotencyKey()));
        } catch (PaymentResultUnknownException e) {
            orderService.markNeedsCheck(order, paymentKey);
            return ConfirmOutcome.NEEDS_CHECK;
        }
        orderService.complete(order, result.paymentKey());
        reservationService.confirm(order.getReservationId());
        return ConfirmOutcome.CONFIRMED;
    }
}
