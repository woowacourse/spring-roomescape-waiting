package roomescape.payment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.ConflictException;
import roomescape.common.exception.NotFoundException;
import roomescape.payment.controller.dto.request.PaymentConfirmRequest;
import roomescape.payment.controller.dto.request.PaymentFailRequest;
import roomescape.payment.controller.dto.response.PaymentConfirmResponse;
import roomescape.payment.controller.dto.response.PaymentFailResponse;
import roomescape.payment.domain.PaymentConfirmation;
import roomescape.payment.domain.PaymentGateway;
import roomescape.payment.domain.PaymentOrder;
import roomescape.payment.domain.PaymentResult;
import roomescape.payment.repository.PaymentOrderRepository;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.repository.ReservationRepository;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentOrderRepository paymentOrderRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentGateway paymentGateway;

    @Transactional
    public PaymentConfirmResponse confirm(final PaymentConfirmRequest request) {
        final PaymentOrder paymentOrder = getPaymentOrder(request.orderId());
        paymentOrder.validateSameAmount(request.amount());

        if (paymentOrder.isCompleted()) {
            validateSamePaymentKey(paymentOrder, request.paymentKey());
            return toResponse(paymentOrder);
        }

        final PaymentResult paymentResult = paymentGateway.confirm(
                new PaymentConfirmation(
                        request.paymentKey(),
                        request.orderId(),
                        request.amount(),
                        paymentOrder.getIdempotencyKey()
                )
        );

        completePayment(paymentOrder, paymentResult);

        return new PaymentConfirmResponse(
                paymentResult.orderId(),
                paymentResult.amount(),
                paymentResult.paymentKey(),
                ReservationStatus.CONFIRMED.name()
        );
    }

    @Transactional
    public PaymentFailResponse fail(final PaymentFailRequest request) {
        if (request.orderId() == null || request.orderId().isBlank()) {
            return new PaymentFailResponse(request.code(), request.message(), request.orderId());
        }

        paymentOrderRepository.findByOrderId(request.orderId())
                .filter(paymentOrder -> !paymentOrder.isCompleted())
                .ifPresent(paymentOrder -> {
                    paymentOrderRepository.deleteByOrderId(paymentOrder.getOrderId());
                    reservationRepository.deletePendingById(paymentOrder.getReservationId());
                });

        return new PaymentFailResponse(request.code(), request.message(), request.orderId());
    }

    public String getClientKey() {
        return paymentGateway.clientKey();
    }

    private PaymentOrder getPaymentOrder(final String orderId) {
        return paymentOrderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 결제 주문입니다."));
    }

    private void validateSamePaymentKey(final PaymentOrder paymentOrder, final String paymentKey) {
        if (!paymentKey.equals(paymentOrder.getPaymentKey())) {
            throw new ConflictException("이미 다른 결제로 승인된 주문입니다.");
        }
    }

    private void completePayment(final PaymentOrder paymentOrder, final PaymentResult paymentResult) {
        final boolean completed = paymentOrderRepository.complete(paymentOrder.getOrderId(), paymentResult.paymentKey());
        final boolean reservationConfirmed = reservationRepository.confirm(paymentOrder.getReservationId());

        if (!completed || !reservationConfirmed) {
            throw new ConflictException("결제 승인 상태가 이미 변경되었습니다.");
        }
    }

    private PaymentConfirmResponse toResponse(final PaymentOrder paymentOrder) {
        return new PaymentConfirmResponse(
                paymentOrder.getOrderId(),
                paymentOrder.getAmount(),
                paymentOrder.getPaymentKey(),
                ReservationStatus.CONFIRMED.name()
        );
    }
}
