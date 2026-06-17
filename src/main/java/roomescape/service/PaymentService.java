package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.client.PaymentAmountMismatchException;
import roomescape.client.PaymentAlreadyProcessedException;
import roomescape.client.PaymentConfirmation;
import roomescape.client.PaymentConfirmationUnknownException;
import roomescape.client.PaymentFailureException;
import roomescape.client.PaymentGateway;
import roomescape.client.PaymentGatewayRetryableException;
import roomescape.client.PaymentResult;
import roomescape.domain.PaymentOrder;
import roomescape.repository.PaymentOrderRepository;
import roomescape.repository.ReservationRepository;

@Service
public class PaymentService {

    private final PaymentOrderRepository paymentOrderRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentGateway paymentGateway;

    public PaymentService(
            PaymentOrderRepository paymentOrderRepository,
            ReservationRepository reservationRepository,
            PaymentGateway paymentGateway
    ) {
        this.paymentOrderRepository = paymentOrderRepository;
        this.reservationRepository = reservationRepository;
        this.paymentGateway = paymentGateway;
    }

    @Transactional
    public PaymentResult confirm(String paymentKey, String orderId, Long amount) {
        PaymentOrder order = paymentOrderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));
        if (!order.amount().equals(amount)) {
            throw new PaymentAmountMismatchException(order.amount(), amount);
        }

        PaymentResult result;
        try {
            result = paymentGateway.confirm(
                    new PaymentConfirmation(paymentKey, orderId, amount, order.idempotencyKey())
            );
        } catch (PaymentConfirmationUnknownException e) {
            paymentOrderRepository.markUnknown(orderId, paymentKey);
            throw e;
        } catch (PaymentFailureException e) {
            paymentOrderRepository.markFailed(orderId);
            throw e;
        } catch (PaymentGatewayRetryableException | PaymentAlreadyProcessedException e) {
            paymentOrderRepository.markUnknown(orderId, paymentKey);
            throw e;
        }
        paymentOrderRepository.complete(orderId, result.paymentKey());
        reservationRepository.confirm(order.reservationId());
        return result;
    }

    @Transactional
    public void cancelPending(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            return;
        }
        paymentOrderRepository.findByOrderId(orderId)
                .ifPresent(order -> paymentOrderRepository.markFailed(orderId));
    }
}
