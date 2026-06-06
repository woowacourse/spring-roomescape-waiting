package roomescape.domain.reservation;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository {

    List<Reservation> findAll();

    Optional<Reservation> findById(Long id);

    List<Reservation> findAllBySlotIdOrderByReservedAt(Long slotId);

    List<Reservation> findAllReservationsByUserId(Long userId);

    Reservation save(Reservation userReservation);

    void batchUpdate(List<Reservation> reservations);

    int deleteById(Long id);

    boolean existsBySlotIdAndUserId(Long slotId, Long userId);
}
