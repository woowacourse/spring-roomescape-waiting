package roomescape.reservation.domain.repository;

import java.util.Optional;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.domain.Waiting;

public interface WaitingRepository {
    Optional<ReservationSlot> findSlotById(Long id);

    Optional<Waiting> findFirstBySlot(ReservationSlot slot);

    Waiting save(Waiting waiting);

    Long getRank(Waiting waiting);

    Integer delete(Long id);
}
