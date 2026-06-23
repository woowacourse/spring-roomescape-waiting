package roomescape.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Payment;
import roomescape.domain.PaymentStatus;
import roomescape.domain.Reservation;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;
import roomescape.repository.PaymentRepository;

@Service
@Transactional
public class PaymentService {

    private static final long RESERVATION_AMOUNT = 20_000L;

    private final ReservationService reservationService;
    private final PaymentRepository paymentRepository;

    public PaymentService(ReservationService reservationService, PaymentRepository paymentRepository) {
        this.reservationService = reservationService;
        this.paymentRepository = paymentRepository;
    }

    public Payment createForReservation(String name, LocalDate date, Long timeId, Long themeId, LocalDateTime now) {
        Reservation reservation = reservationService.createPendingByUser(name, date, timeId, themeId, now);
        return paymentRepository.insert(Payment.ready(reservation.getId(), RESERVATION_AMOUNT));
    }

    public Payment retryForReservation(Long reservationId, String name, LocalDateTime now) {
        Reservation reservation = reservationService.findPendingByUser(reservationId, name, now);
        paymentRepository.findLatestByReservationId(reservation.getId())
                .ifPresent(this::validateRetryablePayment);
        return paymentRepository.insert(Payment.ready(reservation.getId(), RESERVATION_AMOUNT));
    }

    @Transactional(readOnly = true)
    public Payment getReadyPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RoomescapeException(ErrorCode.NOT_FOUND, "존재하지 않는 결제입니다."));
        Reservation reservation = reservationService.findById(payment.getReservationId());
        if (payment.getStatus() != PaymentStatus.READY || !reservation.isPending()) {
            throw new RoomescapeException(ErrorCode.PAYMENT_CHECKOUT_NOT_ALLOWED,
                    "결제를 진행할 수 없는 상태입니다.");
        }
        return payment;
    }

    private void validateRetryablePayment(Payment payment) {
        if (payment.getStatus() != PaymentStatus.FAILED && payment.getStatus() != PaymentStatus.CANCELED) {
            throw new RoomescapeException(ErrorCode.PAYMENT_RETRY_NOT_ALLOWED,
                    "진행 중인 결제가 있어 재결제할 수 없습니다.");
        }
    }
}
