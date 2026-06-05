package roomescape.waiting.application.port.out;

import java.time.LocalDate;
import java.util.Set;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.application.port.out.projection.WaitingDetailProjection;

import java.util.List;
import java.util.Optional;

public interface WaitingRepository {
    Waiting save(Waiting waiting);

    Optional<Waiting> findById(long waitingId);

    Set<Long> findTimeIdByDateAndThemeId(LocalDate date, long themeId);

    boolean existsBySlotIdAndMemberId(long memberId, long slotId);

    boolean existsBySlotId(long slotId);

    List<Waiting> findAllBySlotIdOrderById(long slotId);

    List<Waiting> findAllBySlotIds(List<Long> slotIds);

    List<WaitingDetailProjection> findAllWaitingDetailsByMemberId(long memberId);

    void deleteById(long waitingId);
}
