package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import roomescape.domain.Payment;
import roomescape.domain.PaymentConfirmation;
import roomescape.domain.PaymentGateway;
import roomescape.domain.PaymentOrder;
import roomescape.domain.PaymentResult;
import roomescape.domain.repository.PaymentOrderRepository;
import roomescape.domain.repository.PaymentRepository;
import roomescape.domain.repository.ReservationSlotRepository;
import roomescape.dto.PaymentConfirmRequest;
import roomescape.exception.CustomException;
import roomescape.exception.ErrorCode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
public class PaymentService {
    private static final Logger log =
            LoggerFactory.getLogger(PaymentService.class);

    private final PaymentOrderRepository paymentOrderRepository;
    private final PaymentRepository paymentRepository;
    private final ReservationSlotRepository reservationSlotRepository;
    private final PaymentGateway paymentGateway;

    public PaymentService(
            PaymentOrderRepository paymentOrderRepository,
            PaymentRepository paymentRepository,
            ReservationSlotRepository reservationSlotRepository,
            PaymentGateway paymentGateway
    ) {
        this.paymentOrderRepository = paymentOrderRepository;
        this.paymentRepository = paymentRepository;
        this.reservationSlotRepository = reservationSlotRepository;
        this.paymentGateway = paymentGateway;
    }

    @Transactional
    public void confirm(PaymentConfirmRequest request) {
        PaymentOrder paymentOrder = paymentOrderRepository.getByOrderId(request.orderId());
        if (!paymentOrder.getAmount().equals(request.amount())) {
            log.warn("Payment amount mismatch. expected={}, actual={}", paymentOrder.getAmount(), request.amount());
            throw new CustomException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        PaymentResult result = paymentGateway.confirm(
                new PaymentConfirmation(request.paymentKey(), request.orderId(), request.amount())
        );
        paymentRepository.save(Payment.create(paymentOrder.getId(), result.paymentKey(), result.amount()));
        reservationSlotRepository.confirmPayment(paymentOrder.getReservationId());
    }
}
