package roomescape.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.OrderId;
import roomescape.domain.Payment;
import roomescape.domain.PaymentConfirmation;
import roomescape.domain.PaymentGateway;
import roomescape.domain.PaymentResult;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorCode;
import roomescape.repository.PaymentRepository;
import roomescape.repository.ReservationRepository;
import roomescape.service.dto.ReservationConfirmationEvent;
import roomescape.service.dto.command.PaymentCreateCommand;
import roomescape.service.dto.command.PaymentSuccessCommand;
import roomescape.service.dto.result.PaymentConfirmResult;
import roomescape.service.dto.result.PaymentReadyResult;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final PaymentGateway paymentGateway;

    public PaymentReadyResult create(final PaymentCreateCommand command) {
        if (!reservationRepository.existsById(command.reservationId())) {
            throw new BusinessException(ErrorCode.RESERVATION_NOT_FOUND);
        }
        final OrderId orderId = OrderId.generate();
        final Payment payment = Payment.prepare(
                orderId,
                command.reservationId(),
                command.price()
        );
        final Payment savedPayment = paymentRepository.save(payment);
        return PaymentReadyResult.from(savedPayment);
    }

    @Transactional
    public PaymentConfirmResult confirm(final PaymentSuccessCommand command) {
        final OrderId orderId = new OrderId(command.orderId());
        final Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));
        final String paymentKey = command.paymentKey();
        if (!payment.isSameAmount(command.amount())) {
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }
        final Payment updatedPayment = payment.confirm(paymentKey);
        paymentRepository.update(updatedPayment);

        eventPublisher.publishEvent(new ReservationConfirmationEvent(
                updatedPayment.getReservationId()
        ));

        final PaymentConfirmation confirmation = new PaymentConfirmation(
                updatedPayment.getPaymentKey(), updatedPayment.getOrderId().id(), updatedPayment.getAmount()
        );
        final PaymentResult paymentResult = paymentGateway.confirm(confirmation);

        return new PaymentConfirmResult(paymentResult.orderId(), paymentResult.approvedAmount(), paymentResult.paymentKey());
    }
}
