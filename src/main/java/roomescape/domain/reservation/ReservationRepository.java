package roomescape.domain.reservation;

import roomescape.domain.DomainErrorCode;
import roomescape.domain.RoomEscapeException;

import java.util.Optional;

import static roomescape.domain.DomainErrorCode.RESOURCE_NOT_FOUND;


public interface ReservationRepository {
    Reservations findAll();

    Optional<Reservation> findById(Long id);

    Reservations findByName(String name);

    Reservations findBySlotId(Long slotId);

    Reservation save(Reservation reservation);

    Reservation update(Long id, Reservation reservation);

    void updateStatusById(Long id, Status status);

    void deleteById(Long id);

    boolean existsById(Long id);

    default Reservation getById(Long id) {
        return findById(id)
                .orElseThrow(() -> new RoomEscapeException(RESOURCE_NOT_FOUND, "해당 예약을 찾을 수 없습니다. : " + id));
    }
}
