package roomescape.domain.reservation;

import java.util.List;
import java.util.Optional;
import roomescape.domain.reservation.dto.ReservationCountResult;

public interface ReservationRepository {

    Reservation save(Reservation userReservation);

    List<Reservation> findAll();

    Optional<Reservation> findById(Long id);

    List<Reservation> findReservations(String username);

    Integer countByReservationSlotId(Long reservationSlotId);

    List<Reservation> findAllBySlotIdOrderByWaitingNumber(Long reservationId);

    Optional<Reservation> update(Long id, Reservation updatedReservation);

    boolean existsActiveByUserIdAndReservationId(Long userId, Long reservationId);

    void deleteById(Long id);

    List<ReservationCountResult> countReservation(Long themeId, Long dateId);

    void batchUpdate(List<Reservation> reservations);
}
