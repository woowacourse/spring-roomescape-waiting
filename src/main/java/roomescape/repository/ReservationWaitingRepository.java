package roomescape.repository;

import roomescape.domain.ReservationWaiting;

import java.util.Optional;

public interface ReservationWaitingRepository {

    ReservationWaiting save(ReservationWaiting reservationWaiting);

    boolean existBy(String name, Long reservationId);

    Optional<ReservationWaiting> findById(Long id);

    void deleteById(Long id);
}
