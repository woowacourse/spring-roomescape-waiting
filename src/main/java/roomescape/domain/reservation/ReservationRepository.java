package roomescape.domain.reservation;

import roomescape.domain.CommonRepository;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends CommonRepository<Reservation> {
    Reservations findAll();

    Reservations findByName(String name);

    Reservations findBySlotId(Long slotId);

    Optional<Reservation> findFirstWaitingBySlotId(Long slotId);

    boolean existsBySlotIdAndName(Long slotId, String name);

    boolean existsApprovedBySlotId(Long slotId);

    Reservation update(Long id, Reservation reservation);

    void updateStatusById(Long id, Status status);
}
