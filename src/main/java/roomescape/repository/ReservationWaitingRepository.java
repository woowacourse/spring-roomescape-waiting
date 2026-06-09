package roomescape.repository;

import java.util.Optional;
import roomescape.domain.Member;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.Slot;

public interface ReservationWaitingRepository {

    ReservationWaiting save(ReservationWaiting reservationWaiting);

    boolean existsBy(Member member, Slot slot);

    Optional<ReservationWaiting> findById(Long id);

    Optional<ReservationWaiting> findFirstBySlot(Slot slot);

    void deleteById(Long id);
}
