package roomescape.repository;

import java.util.List;
import java.util.Optional;
import roomescape.domain.ReservationWaiting;

public interface ReservationWaitingRepository {

    ReservationWaiting save(ReservationWaiting reservationWaiting);

    boolean existBy(String name, Long reservationId);

    Optional<ReservationWaiting> findById(Long id);

    List<ReservationWaiting> findByName(String name);

    void deleteById(Long id);
}
