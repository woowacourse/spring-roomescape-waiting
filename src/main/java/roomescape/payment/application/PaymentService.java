package roomescape.payment.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.payment.application.dto.OrderInfo;
import roomescape.payment.application.dto.PaymentCancel;
import roomescape.payment.application.dto.PaymentCancelCommand;
import roomescape.payment.application.dto.PaymentConfirmation;
import roomescape.payment.application.dto.PaymentResult;
import roomescape.payment.application.exception.OrderUpdateException;
import roomescape.payment.application.exception.PaymentAmountMismatchException;
import roomescape.payment.application.exception.PaymentUnauthorizedException;
import roomescape.payment.domain.OrderStatus;
import roomescape.payment.infra.client.exception.TossBusinessException;
import roomescape.payment.infra.client.exception.TossInfrastructureException;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentGateway paymentGateway;
    private final OrderService orderService;

    @Transactional
    public PaymentResult confirm(String paymentKey, String orderId, Long amount) {
        OrderInfo order = orderService.getOrder(orderId);
        try {
            validateAmount(amount, order);
        } catch (PaymentAmountMismatchException e) {
            orderService.fail(order.orderId());
            throw e;
        }

        PaymentResult result = executeConfirm(paymentKey, orderId, amount);

        OrderStatus actualStatus = result.status();
        if (!actualStatus.equals(OrderStatus.COMPLETED)) {
            orderService.fail(orderId);
            throw new OrderUpdateException("결제가 정상적으로 완료되지 않았습니다. 현재 상태: " + actualStatus.name());
        }
        return orderLocally(paymentKey, orderId, amount, order, result);
    }

    @Transactional
    public PaymentResult cancel(String orderId, PaymentCancelCommand command) {
        OrderInfo order = orderService.getOrder(orderId);
        if (!order.reservation().name().equals(command.name())) {
            throw new PaymentUnauthorizedException("결제 취소 권한이 없습니다.");
        }
        validateAmount(command.cancelAmount(), order);
        PaymentResult result = cancelTossPayment(command.cancelAmount(), command.cancelReason(),
                order.paymentKey());
        orderService.cancel(orderId);
        return result;
    }

    @Transactional
    public void cancelBySystem(String orderId, String reason) {
        OrderInfo order = orderService.getOrder(orderId);
        cancelTossPayment(order.amount(), reason, order.paymentKey());
        orderService.cancel(orderId);
    }

    private PaymentResult orderLocally(String paymentKey, String orderId, Long amount, OrderInfo order,
                                           PaymentResult result) {
        try {
            orderService.complete(orderId, paymentKey);
            return result;
        } catch (OrderUpdateException e) {
            log.error("DB 갱신 실패로 토스 결제를 자동 취소합니다. OrderId: {}", orderId, e);
            cancelTossPayment(amount, "내부 서버 오류로 인한 자동 환불", paymentKey);
            throw new OrderUpdateException("시스템 오류로 결제가 자동 취소(환불)되었습니다.", e);
        }
    }

    private PaymentResult cancelTossPayment(Long amount, String cancelReason, String paymentKey) {
        return paymentGateway.cancel(PaymentCancel.builder()
                .cancelAmount(amount)
                .cancelReason(cancelReason)
                .paymentKey(paymentKey)
                .build());
    }

    private PaymentResult executeConfirm(String paymentKey, String orderId, Long amount) {
            try {
                return paymentGateway.confirm(PaymentConfirmation.builder()
                        .paymentKey(paymentKey)
                        .orderId(orderId)
                        .amount(amount)
                        .build()
                );
            } catch (TossInfrastructureException e) {
                log.error("결제망 통신 지연 및 최종 재시도 실패. OrderId: {}", orderId);
                throw new OrderUpdateException("결제망 통신 지연으로 처리할 수 없습니다. 잠시 후 다시 시도해주세요.", e);
            } catch (TossBusinessException e) {
                orderService.fail(orderId);
                throw e;
            }
    }

    private void validateAmount(Long amount, OrderInfo order) {
        if (!order.amount().equals(amount)) {
            throw new PaymentAmountMismatchException(order.amount(), amount);
        }
    }
}
