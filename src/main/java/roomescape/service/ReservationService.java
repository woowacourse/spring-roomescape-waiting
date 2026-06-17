package roomescape.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.exception.DomainErrorCode;
import roomescape.domain.exception.RoomEscapeException;
import roomescape.payment.OrderIdGenerator;
import roomescape.repository.ReservationRepository;
import roomescape.service.dto.request.ServiceReservationCreateRequest;

@Service
public class ReservationService {

    private static final long RESERVATION_AMOUNT = 50_000L;

    private final ReservationRepository reservationRepository;
    private final OrderIdGenerator orderIdGenerator;

    public ReservationService(ReservationRepository reservationRepository, OrderIdGenerator orderIdGenerator) {
        this.reservationRepository = reservationRepository;
        this.orderIdGenerator = orderIdGenerator;
    }

    public Reservation save(ServiceReservationCreateRequest request, ReservationTime reservationTime, Theme theme) {
        Reservation reservationWithoutId = request.toReservation(reservationTime, theme);
        return reservationRepository.save(reservationWithoutId);
    }

    public Reservation savePending(ServiceReservationCreateRequest request, ReservationTime reservationTime, Theme theme) {
        String orderId = orderIdGenerator.generate();
        Reservation reservationWithoutId = Reservation.pending(request.name(), request.reservationDate(),
                reservationTime, theme, orderId, UUID.randomUUID().toString(), RESERVATION_AMOUNT);
        return reservationRepository.save(reservationWithoutId);
    }

    public List<Reservation> findByName(String name) {
        return reservationRepository.findByName(name);
    }

    public List<Reservation> findPaymentHistoryByName(String name) {
        return reservationRepository.findPaymentHistoryByName(name);
    }

    public List<Reservation> findAll() {
        return reservationRepository.findAll();
    }

    public Reservation findReservation(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RoomEscapeException(DomainErrorCode.NOT_FOUND_RESERVATION));
    }

    public void delete(Long id) {
        reservationRepository.delete(id);
    }

    public Optional<Reservation> findBySlot(LocalDate date, Long timeId, Long themeId) {
        return reservationRepository.findBySlot(date, timeId, themeId);
    }

    public void lockById(Long id) {
        reservationRepository.lockById(id)
                .orElseThrow(() -> new RoomEscapeException(DomainErrorCode.NOT_FOUND_RESERVATION));
    }

    public Optional<Long> lockBySlot(LocalDate date, Long timeId, Long themeId) {
        return reservationRepository.lockBySlot(date, timeId, themeId);
    }

    public void lockByOrderId(String orderId) {
        reservationRepository.lockByOrderId(orderId)
                .orElseThrow(() -> new RoomEscapeException(DomainErrorCode.NOT_FOUND_PAYMENT_ORDER));
    }

    public Reservation findByOrderId(String orderId) {
        return reservationRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RoomEscapeException(DomainErrorCode.NOT_FOUND_PAYMENT_ORDER));
    }

    public Reservation confirmPayment(String orderId, String paymentKey) {
        return reservationRepository.confirmPayment(orderId, paymentKey);
    }

    public Reservation markPaymentUnknown(String orderId) {
        return reservationRepository.markPaymentUnknown(orderId);
    }

    public void deletePendingByOrderId(String orderId) {
        reservationRepository.deletePendingByOrderId(orderId);
    }

    public void deleteStalePendingBefore(LocalDateTime expiresBefore) {
        reservationRepository.deleteStalePendingBefore(expiresBefore);
    }

    public void validateReferencedTheme(Long themeId) {
        if (reservationRepository.existsByThemeId(themeId)) {
            throw new RoomEscapeException(DomainErrorCode.REFERENCED_THEME);
        }
    }

    public void validateReferencedTime(Long id) {
        if (reservationRepository.existsByTimeId(id)) {
            throw new RoomEscapeException(DomainErrorCode.REFERENCED_TIME);
        }
    }
}
