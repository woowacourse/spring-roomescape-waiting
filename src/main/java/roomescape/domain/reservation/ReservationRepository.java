package roomescape.domain.reservation;

import java.util.List;
import java.util.Optional;
import roomescape.domain.reservation.dto.ReservationCountResult;

public interface ReservationRepository {

    Reservation save(Reservation userReservation);

    List<Reservation> findAll();

    Optional<Reservation> findById(Long id);

    List<Reservation> findReservations(String username);

    Long countByReservationSlotId(Long reservationSlotId);

    List<Reservation> findAllByReservationIdOrder(Long reservationId);

    Optional<Reservation> update(Long id, Reservation updatedReservation);

    void updateStatus(Long id, ReservationStatus status);

    boolean existsActiveByUserIdAndReservationId(Long userId, Long reservationId);

    void updateWaitingNumbers(List<Reservation> userReservations);

    void updateAllStatus(List<Reservation> userReservations);

    void deleteById(Long id);

    List<ReservationCountResult> countReservation(Long themeId, Long dateId);
}
