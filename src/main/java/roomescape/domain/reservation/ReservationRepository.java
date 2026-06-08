package roomescape.domain.reservation;

import roomescape.common.exception.NotFoundException;

import java.util.Optional;


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
                .orElseThrow(() -> new NotFoundException("존재하지 않는 예약입니다. 입력을 확인해 주세요."));
    }
}
