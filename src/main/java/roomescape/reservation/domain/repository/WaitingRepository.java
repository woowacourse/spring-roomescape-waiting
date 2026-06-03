package roomescape.reservation.domain.repository;

import java.util.Optional;
import roomescape.reservation.domain.Rank;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.domain.Waiting;

public interface WaitingRepository {
    Optional<Waiting> findById(Long id);

    Optional<Waiting> findFirstBySlot(ReservationSlot slot);

    Waiting save(Waiting waiting);

    Integer delete(Long id);

    void rebalanceRank(ReservationSlot slot, Rank rank);

    int countBySlot(ReservationSlot slot);

    Integer postpone(Waiting waiting, Waiting postponedWaiting);
}
