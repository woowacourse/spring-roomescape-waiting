package roomescape.domain.reservation;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository {

    List<Reservation> findAll();

    Optional<Reservation> findById(Long id);

    List<Reservation> findAllBySlotIdOrderByWaitingNumber(Long reservationId);

    List<Reservation> findAllReservationsByUsername(String username);

    Reservation save(Reservation userReservation);

    Optional<Reservation> update(Long id, Reservation updatedReservation);

    void batchUpdate(List<Reservation> reservations);

    int updateSlot(Long id, ReservationSlot newSlot);

    void deleteById(Long id);

    boolean existsByUserIdAndSlotId(Long userId, Long slotId);

    boolean existsActiveByUserIdAndReservationId(Long userId, Long reservationId);

    List<ReservationCountResult> countWaitingReservationsByThemeAndDate(Long themeId, Long dateId);

    Integer countByReservationSlotId(Long reservationSlotId);

    default Reservation findByIdOrThrow(Long id) {
        return findById(id)
                .orElseThrow();
    }
}
