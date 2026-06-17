package roomescape.reservation.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Status;
import roomescape.reservation.repository.dto.ReservationWithWaitingOrder;
import roomescape.time.domain.ReservationTime;

public interface ReservationRepository {
    List<Reservation> findAll();

    Optional<Reservation> findById(Long id);

    Optional<Reservation> findByIdForUpdate(Long id);

    Reservation save(Reservation reservation);

    boolean update(Long id, Long timeId, LocalDateTime now, Status status);

    List<Long> findTimeIdsByThemeIdAndDate(Long themeId, LocalDate date);

    boolean deleteById(Long id);

    boolean hasConfirmedReservation(Long themeId, ReservationTime time);

    boolean existsByTimeId(Long timeId);

    Optional<Long> findEarliestWaiting(Long timeId, Long themeId);

    boolean promoteToReserved(Long waitingId);

    boolean confirmPayment(Long reservationId, String paymentKey);

    java.util.Optional<Reservation> findByOrderId(String orderId);

    List<ReservationWithWaitingOrder> findAllByName(String name);

    boolean isDuplicatedWithName(String name, Long themeId, ReservationTime time);
}
