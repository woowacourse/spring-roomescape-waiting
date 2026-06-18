package roomescape.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.domain.OrderId;
import roomescape.domain.Payment;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorCode;
import roomescape.repository.PaymentRepository;
import roomescape.repository.ReservationRepository;
import roomescape.service.dto.command.PaymentCreateCommand;
import roomescape.service.dto.result.PaymentReadyResult;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;

    public PaymentReadyResult create(final PaymentCreateCommand data) {
        if (!reservationRepository.existsById(data.reservationId())) {
            throw new BusinessException(ErrorCode.RESERVATION_NOT_FOUND);
        }
        final OrderId orderId = OrderId.generate();
        final Payment payment = Payment.prepare(
                orderId,
                data.reservationId(),
                data.price()
        );
        final Payment savedPayment = paymentRepository.save(payment);
        return PaymentReadyResult.from(savedPayment);
    }
}
