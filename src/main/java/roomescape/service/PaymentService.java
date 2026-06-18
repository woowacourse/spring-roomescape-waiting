package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.controller.dto.ReservationPaymentResponse;
import roomescape.domain.Reservation;
import roomescape.domain.ThemeSlot;
import roomescape.domain.payment.Payment;
import roomescape.domain.payment.PaymentConfirmation;
import roomescape.domain.payment.PaymentGateway;
import roomescape.domain.payment.PaymentResult;
import roomescape.domain.payment.PaymentStatus;
import roomescape.global.exception.CustomException;
import roomescape.global.exception.ErrorCode;
import roomescape.repository.PaymentRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ThemeSlotRepository;

import java.util.List;
import java.util.Optional;

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

        ThemeSlot themeSlot = themeSlotRepository.findById(reservation.getThemeSlotId())
                .orElseThrow(() -> new CustomException(ErrorCode.THEME_SLOT_NOT_FOUND));

        if (themeSlot.isReserved()) {
            throw new CustomException(ErrorCode.PAYMENT_SLOT_ALREADY_CONFIRMED);
        }

        try {
            PaymentResult result = paymentGateway.confirm(confirmation);

            Payment payment = paymentRepository.save(
                    new Payment(reservation.getId(), result.paymentKey(), result.orderId(), result.amount(), PaymentStatus.CONFIRMED)
            );

            reservation.confirm();
            reservationRepository.updateStatus(reservation);

            ThemeSlot reservedSlot = new ThemeSlot(
                    reservation.getTheme(), reservation.getDate(), reservation.getTime(), true
            );
            themeSlotRepository.update(reservedSlot);

            return payment;

        } catch (CustomException e) {
            if (e.getErrorCode() == ErrorCode.PAYMENT_READ_TIMEOUT) {
                return paymentRepository.save(
                        new Payment(reservation.getId(), null, confirmation.orderId(), confirmation.amount(), PaymentStatus.UNCERTAIN)
                );
            }
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public List<ReservationPaymentResponse> getPaymentHistory(String name) {
        return reservationRepository.findByName(name).stream()
                .map(reservation -> {
                    Optional<Payment> payment = paymentRepository.findByReservationId(reservation.getId());
                    return payment
                            .map(p -> ReservationPaymentResponse.of(reservation, p))
                            .orElseGet(() -> ReservationPaymentResponse.withoutPayment(reservation));
                })
                .toList();
    }
}
