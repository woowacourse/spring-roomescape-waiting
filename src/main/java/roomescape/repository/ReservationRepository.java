package roomescape.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import roomescape.domain.Reservation;

public interface ReservationRepository {

    Reservation save(Reservation reservationWithoutId);

    Optional<Reservation> findById(Long id);

    Optional<Reservation> findBySlot(LocalDate date, Long timeId, Long themeId);

    Optional<Long> lockById(Long id);

    Optional<Long> lockBySlot(LocalDate date, Long timeId, Long themeId);

    Optional<Long> lockByOrderId(String orderId);

    Optional<Reservation> findByOrderId(String orderId);

    Optional<Reservation> findPendingByOrderId(String orderId);

    Reservation startPaymentConfirmation(String orderId);

    Reservation releasePaymentConfirmation(String orderId);

    Reservation confirmPayment(String orderId, String paymentKey);

    Reservation markPaymentUnknown(String orderId);

    void deletePendingByOrderId(String orderId);

    void deleteStalePendingBefore(LocalDateTime expiresBefore);

    List<Reservation> findStalePendingBefore(LocalDateTime expiresBefore);

    List<Reservation> findByName(String name);

    List<Reservation> findPaymentHistoryByName(String name);

    List<Reservation> findAll();

    void delete(Long id);

    boolean existsByTimeId(Long timeId);

    boolean existsByThemeId(Long themeId);
}
