package roomescape.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Payment;
import roomescape.domain.Reservation;
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
}
