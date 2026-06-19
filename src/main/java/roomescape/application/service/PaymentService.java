package roomescape.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.service.command.PaymentConfirmCommand;
import roomescape.domain.payment.PaymentHistory;
import roomescape.domain.payment.PaymentStatus;
import roomescape.event.publisher.EventPublisher;
import roomescape.exception.PaymentException;
import roomescape.persistence.PaymentHistoryRepository;
import roomescape.pg.PaymentConfirmation;
import roomescape.pg.PaymentGateway;
import roomescape.pg.PaymentGatewayResult;
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
        validatePaymentAmount(command, expectedAmount);
        PaymentGatewayResult result = paymentGateway.confirm(new PaymentConfirmation(
                command.paymentKey(),
                command.orderId(),
                command.amount()
        ));
        return handleGatewayResult(command, result);
    }

    private void validatePaymentAmount(PaymentConfirmCommand command, long expectedAmount) {
        if (expectedAmount != command.amount()) {
            record(command, PaymentStatus.FAILED);
            throw new PaymentException.AmountMismatch(expectedAmount, command.amount());
        }
    }

    private PaymentResult handleGatewayResult(PaymentConfirmCommand command, PaymentGatewayResult result) {
        return switch (result) {
            case PaymentGatewayResult.Approved approved -> approve(approved.payment());
            case PaymentGatewayResult.Unknown ignored -> {
                record(command, PaymentStatus.CHECK_REQUIRED);
                throw new PaymentException.CheckRequired();
            }
            case PaymentGatewayResult.Rejected rejected -> {
                record(command, PaymentStatus.FAILED);
                throw new PaymentException.Rejected(rejected.message());
            }
        };
    }

    private PaymentResult approve(PaymentResult payment) {
        PaymentHistory paymentHistory = PaymentHistory.record(
                payment.orderId(),
                payment.paymentKey(),
                payment.approvedAmount(),
                payment.status()
        );
        paymentHistoryRepository.save(paymentHistory);
        eventPublisher.publishEvents(paymentHistory.pullEvents());
        return payment;
    }

    private void record(PaymentConfirmCommand command, PaymentStatus status) {
        paymentHistoryRepository.save(PaymentHistory.record(
                command.orderId(),
                command.paymentKey(),
                command.amount(),
                status
        ));
    }

}
