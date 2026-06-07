package roomescape.domain.reservation;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository {

    List<Reservation> findAll();

    List<Reservation> findAllBySlotIdOrderByReservedAt(Long slotId);

    List<Reservation> findAllReservationsByUserId(Long userId);

    Optional<Reservation> findById(Long id);

    Optional<Reservation> findByIdAndUsername(Long id, String username);

    Reservation save(Reservation userReservation);

    Reservation update(Reservation reservation);

    void batchUpdate(List<Reservation> reservations);

    int deleteById(Long id);

    boolean existsBySlotIdAndUserId(Long slotId, Long userId);
}
