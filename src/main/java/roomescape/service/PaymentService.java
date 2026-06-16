package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.ThemeSlot;
import roomescape.domain.payment.Payment;
import roomescape.domain.payment.PaymentConfirmation;
import roomescape.domain.payment.PaymentGateway;
import roomescape.domain.payment.PaymentResult;
import roomescape.global.exception.CustomException;
import roomescape.global.exception.ErrorCode;
import roomescape.repository.PaymentRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ThemeSlotRepository;

@Service
public class PaymentService {

    private final PaymentGateway paymentGateway;
    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final ThemeSlotRepository themeSlotRepository;

    public PaymentService(
            PaymentGateway paymentGateway,
            ReservationRepository reservationRepository,
            PaymentRepository paymentRepository,
            ThemeSlotRepository themeSlotRepository
    ) {
        this.paymentGateway = paymentGateway;
        this.reservationRepository = reservationRepository;
        this.paymentRepository = paymentRepository;
        this.themeSlotRepository = themeSlotRepository;
    }

    @Transactional
    public void handlePaymentFail(String orderId) {
        if (orderId == null) {
            return;
        }
        reservationRepository.findByOrderId(orderId).ifPresent(reservation -> {
            reservation.cancel();
            reservationRepository.updateStatus(reservation);
        });
    }

    @Transactional
    public Payment confirmPayment(PaymentConfirmation confirmation) {
        Reservation reservation = reservationRepository.findByOrderId(confirmation.orderId())
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));

        if (!confirmation.amount().equals(reservation.getAmount())) {
            throw new CustomException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        PaymentResult result = paymentGateway.confirm(confirmation);

        Payment payment = paymentRepository.save(
                new Payment(reservation.getId(), result.paymentKey(), result.orderId(), result.amount())
        );

        reservation.confirm();
        reservationRepository.updateStatus(reservation);

        ThemeSlot reservedSlot = new ThemeSlot(
                reservation.getTheme(), reservation.getDate(), reservation.getTime(), true
        );
        themeSlotRepository.update(reservedSlot);

        return payment;
    }
}
