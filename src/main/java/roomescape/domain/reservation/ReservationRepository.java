package roomescape.domain.reservation;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository {

    Reservation save(Reservation userReservation);

    List<Reservation> findAll();

    Optional<Reservation> findById(Long id);

    List<Reservation> findByUserId(Long userId);

    Long countByReservationId(Long reservationId);

    List<Reservation> findAllByReservationIdOrder(Long reservationId);

    Optional<Reservation> update(Long id, Reservation userReservation);

    void updateStatus(Long id, WaitingStatus status);

    boolean existsActiveByUserIdAndReservationId(Long userId, Long reservationId);

    void updateWaitingNumbers(List<Reservation> userReservations);
}
