package roomescape.domain.payment;

import org.springframework.stereotype.Service;
import roomescape.domain.payment.dto.PaymentConfirmRequest;
import roomescape.domain.payment.dto.PaymentConfirmResponse;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;

@Service
public class PaymentService {

    private final PaymentClient paymentClient;
    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentClient paymentClient, PaymentRepository paymentRepository) {
        this.paymentClient = paymentClient;
        this.paymentRepository = paymentRepository;
    }

    public PaymentConfirmResponse confirm(PaymentConfirmRequest request) {
        ReservationPayment payment = paymentRepository.findByOrderId(request.orderId())
            .orElseThrow(() -> new RoomescapeException(ErrorCode.PAYMENT_ORDER_NOT_FOUND));
        validateAmount(request, payment);

        if (payment.status() == PaymentStatus.CONFIRMED) {
            return confirmedResponse(payment);
        }

        try {
            PaymentConfirmResponse response = paymentClient.confirm(request, payment.idempotencyKey());
            paymentRepository.updateConfirmed(request.orderId(), response.paymentKey(), response.totalAmount());
            return response;
        } catch (PaymentResultUnknownException exception) {
            paymentRepository.updateRequiresConfirmation(request.orderId(), request.paymentKey());
            throw exception;
        } catch (PaymentConnectionException exception) {
            paymentRepository.updateStatus(request.orderId(), PaymentStatus.FAILED);
            throw exception;
        } catch (PaymentException exception) {
            if ("ALREADY_PROCESSED_PAYMENT".equals(exception.getCode())) {
                paymentRepository.updateRequiresConfirmation(request.orderId(), request.paymentKey());
            } else {
                paymentRepository.updateStatus(request.orderId(), PaymentStatus.FAILED);
            }
            throw exception;
        }
    }

    private void validateAmount(PaymentConfirmRequest request, ReservationPayment payment) {
        if (!payment.amount().equals(request.amount())) {
            throw new RoomescapeException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }
    }

    private PaymentConfirmResponse confirmedResponse(ReservationPayment payment) {
        return new PaymentConfirmResponse(
            payment.paymentKey(),
            payment.orderId(),
            payment.amount(),
            "DONE"
        );
    }
}
