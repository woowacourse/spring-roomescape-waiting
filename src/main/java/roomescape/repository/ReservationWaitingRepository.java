package roomescape.repository;

import java.util.Optional;
import roomescape.domain.Member;
import roomescape.domain.ReservationWaiting;

public interface ReservationWaitingRepository {

    ReservationWaiting save(ReservationWaiting reservationWaiting, Long reservationId);

    boolean existBy(Member member, Long reservationId);

    Optional<ReservationWaiting> findById(Long id);

    void deleteById(Long id);
}
