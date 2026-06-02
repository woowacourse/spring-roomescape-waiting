package roomescape.waiting.infrastructure;

import java.time.LocalDate;
import java.util.Set;
import roomescape.waiting.Waiting;
import roomescape.waiting.infrastructure.projection.WaitingDetailProjection;

import java.util.List;
import java.util.Optional;

public interface WaitingRepository {
    Waiting save(Waiting waiting);

    Optional<Waiting> findById(long waitingId);

    Set<Long> findTimeIdByDateAndThemeId(LocalDate date, long themeId);

    boolean existsBySlotIdAndMemberId(long memberId, long slotId);

    boolean existsBySlotId(long slotId);

    long countBySlotIdAndIdLessThanEqual(long slotId, long waitingId);

    List<WaitingDetailProjection> findAllWaitingDetailsByMemberId(long memberId);

    void deleteById(long waitingId);
}
