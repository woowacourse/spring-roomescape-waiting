package roomescape.payment.application;

import java.time.Clock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.payment.application.dto.PaymentCancel;
import roomescape.payment.application.dto.PaymentCancelCommand;
import roomescape.payment.application.dto.PaymentConfirmation;
import roomescape.payment.application.dto.PaymentResult;
import roomescape.payment.application.exception.OrderUpdateException;
import roomescape.payment.application.exception.PaymentAmountMismatchException;
import roomescape.payment.application.exception.PaymentUnauthorizedException;
import roomescape.payment.domain.Order;
import roomescape.payment.domain.OrderRepository;
import roomescape.payment.domain.OrderStatus;
import roomescape.reservation.application.ReservationReader;
import roomescape.reservation.application.dto.ReservationIntegrationInfo;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PaymentService {

    private final Clock clock;
    private final OrderRepository orderRepository;
    private final PaymentGateway paymentGateway;
    private final ReservationReader reservationReader;

    public PaymentResult confirm(String paymentKey, String orderId, Long amount) {
        Order order = orderRepository.getByOrderId(orderId);
        if (!order.getAmount().equals(amount)) {
            throw new PaymentAmountMismatchException(order.getAmount(), amount);
        }
        PaymentResult result = paymentGateway.confirm(PaymentConfirmation.builder()
                .paymentKey(paymentKey)
                .orderId(orderId)
                .amount(amount)
                .build()
        );
        OrderStatus actualStatus = OrderStatus.fromToss(result.status());
        if (!actualStatus.equals(OrderStatus.COMPLETED)) {
            Order failed = order.fail(clock);
            orderRepository.update(failed);
            throw new OrderUpdateException("결제가 정상적으로 완료되지 않았습니다. 현재 상태: " + actualStatus.name());
        }
        try {
            Order completed = order.complete(paymentKey, clock);
            int affected = orderRepository.update(completed);
            if (affected == 0) {
                throw new OrderUpdateException("주문 갱신 실패 (affected row 0)");
            }
            return result;
        } catch (Exception e) {
            log.error("DB 갱신 실패로 토스 결제를 자동 취소합니다. OrderId: {}", orderId, e);
            PaymentCancel cancel = PaymentCancel.builder()
                    .cancelAmount(amount)
                    .cancelReason("내부 서버 오류로 인한 자동 환불")
                    .paymentKey(paymentKey)
                    .build();
            paymentGateway.cancel(cancel);
            throw new OrderUpdateException("시스템 오류로 결제가 자동 취소(환불)되었습니다.", e);
        }
    }

    public void fail(String orderId) {
        Order order = orderRepository.getByOrderId(orderId);
        Order failed = order.fail(clock);
        int affected = orderRepository.update(failed);
        if (affected == 0) {
            log.error("결제 실패 상태 갱신 중 에러 발생. OrderId: {}", orderId);
            throw new OrderUpdateException("결제 실패 상태 저장에 실패했습니다.");
        }
    }

    public PaymentResult cancel(String orderId, PaymentCancelCommand command) {
        Order order = orderRepository.getByOrderId(orderId);
        ReservationIntegrationInfo reservation = reservationReader.read(order.getReservationId());
        if (!reservation.name().equals(command.name())) {
            throw new PaymentUnauthorizedException("결제 취소 권한이 없습니다.");
        }

        if (!order.getAmount().equals(command.cancelAmount())) {
            throw new PaymentAmountMismatchException(order.getAmount(), command.cancelAmount());
        }

        PaymentCancel cancel = PaymentCancel.builder()
                .cancelAmount(command.cancelAmount())
                .cancelReason(command.cancelReason())
                .paymentKey(order.getPaymentKey())
                .build();

        PaymentResult result = paymentGateway.cancel(cancel);
        Order cancelled = order.cancel(clock);
        orderRepository.update(cancelled);
        return result;
    }

    public void cancelBySystem(String orderId, String reason) {
        Order order = orderRepository.getByOrderId(orderId);

        PaymentCancel cancel = PaymentCancel.builder()
                .cancelAmount(order.getAmount())
                .cancelReason(reason)
                .paymentKey(order.getPaymentKey())
                .build();

        paymentGateway.cancel(cancel);
        Order cancelled = order.cancel(clock);
        orderRepository.update(cancelled);
    }
}
