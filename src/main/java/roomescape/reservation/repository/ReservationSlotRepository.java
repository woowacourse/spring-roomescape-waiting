package roomescape.reservation.repository;

import roomescape.reservation.domain.ReservationSlot;

import java.util.Optional;

public interface ReservationSlotRepository {

    ReservationSlot upsert(ReservationSlot reservationSlot);

    Optional<ReservationSlot> findByIdWithLock(Long id);
}
