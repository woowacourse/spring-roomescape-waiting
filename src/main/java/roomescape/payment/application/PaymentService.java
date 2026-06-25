package roomescape.payment.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import roomescape.common.exception.ErrorCode;
import roomescape.common.exception.RoomEscapeException;
import roomescape.payment.domain.Order;
import roomescape.payment.domain.PaymentConfirmation;
import roomescape.payment.domain.PaymentResult;
import roomescape.payment.dto.PaymentConfirmRequest;
import roomescape.payment.dto.PaymentConfirmResponse;
import roomescape.payment.exception.PaymentErrorCode;
import roomescape.payment.repository.OrderRepository;
import roomescape.reservation.service.ReservationService;

@Service
public class PaymentService {

    private final PaymentGateway paymentGateway;
    private final OrderRepository orderRepository;
    private final ReservationService reservationService;
    private final TransactionTemplate transactionTemplate;

    public PaymentService(PaymentGateway paymentGateway, OrderRepository orderRepository,
                          ReservationService reservationService, PlatformTransactionManager transactionManager) {
        this.paymentGateway = paymentGateway;
        this.orderRepository = orderRepository;
        this.reservationService = reservationService;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    /**
     * 외부 호출(게이트웨이)을 트랜잭션 밖에 두어, 결과가 불명확한 read timeout에도 주문 상태(UNKNOWN)가
     * 커밋되도록 한다. 성공/실패/확인 필요를 각각 짧은 트랜잭션으로 끊어 기록한다.
     */
    public PaymentConfirmResponse confirm(PaymentConfirmRequest request) {
        Order order = orderRepository.findByOrderId(request.orderId())
                .orElseThrow(() -> new RoomEscapeException(PaymentErrorCode.ORDER_NOT_FOUND));
        order.verifyAmount(request.amount());
        if (order.isDone()) {
            return PaymentConfirmResponse.from(order);
        }

        try {
            PaymentResult result = paymentGateway.confirm(new PaymentConfirmation(
                    order.getOrderId().value(), request.paymentKey(), request.amount(), order.getIdempotencyKey()));
            return completeConfirmed(order, result);
        } catch (RoomEscapeException e) {
            recordFailure(order, e.getErrorCode());
            throw e;
        }
    }

    private PaymentConfirmResponse completeConfirmed(Order order, PaymentResult result) {
        return transactionTemplate.execute(status -> {
            Order confirmed = orderRepository.updatePayment(order.confirm(result.paymentKey()));
            reservationService.confirmReservation(confirmed.getReservationId());
            return PaymentConfirmResponse.from(confirmed);
        });
    }

    private void recordFailure(Order order, ErrorCode errorCode) {
        if (errorCode == PaymentErrorCode.PAYMENT_CONNECTION_FAILED) {
            return; // 승인되지 않았고 재시도 가능하므로 결제 대기(PENDING)로 둔다
        }
        if (errorCode == PaymentErrorCode.PAYMENT_RESULT_UNKNOWN) {
            transactionTemplate.executeWithoutResult(status -> orderRepository.updatePayment(order.markUnknown()));
            return;
        }
        transactionTemplate.executeWithoutResult(status -> orderRepository.updatePayment(order.markFailed()));
    }

    public void fail(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            return;
        }
        orderRepository.findByOrderId(orderId)
                .ifPresent(order -> reservationService.cancel(order.getReservationId()));
    }
}
