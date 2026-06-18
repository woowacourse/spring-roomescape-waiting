package roomescape.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.service.command.PaymentConfirmCommand;
import roomescape.domain.payment.PaymentHistory;
import roomescape.event.publisher.EventPublisher;
import roomescape.exception.PaymentException;
import roomescape.persistence.PaymentHistoryRepository;
import roomescape.pg.PaymentConfirmation;
import roomescape.pg.PaymentGateway;
import roomescape.pg.PaymentResult;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentHistoryRepository paymentHistoryRepository;
    private final PaymentGateway paymentGateway;
    private final EventPublisher eventPublisher;

    @Transactional
    public PaymentResult confirm(PaymentConfirmCommand command, long expectedAmount) {
        PaymentResult payment = confirmToPayGateway(command, expectedAmount);
        PaymentHistory paymentHistory = PaymentHistory.approved(
                payment.orderId(),
                payment.paymentKey(),
                payment.approvedAmount(),
                payment.status()
        );
        paymentHistoryRepository.save(paymentHistory);
        eventPublisher.publishEvents(paymentHistory.pullEvents());
        return payment;
    }

    private PaymentResult confirmToPayGateway(PaymentConfirmCommand command, long expectedAmount) {
        validatePaymentAmount(expectedAmount, command.amount());
        return paymentGateway.confirm(new PaymentConfirmation(
                command.paymentKey(),
                command.orderId(),
                command.amount()
        ));
    }

    private void validatePaymentAmount(long orderAmount, long amount) {
        if (orderAmount != amount) {
            throw new PaymentException.AmountMismatch(orderAmount, amount);
        }
    }
}
