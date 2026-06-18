package roomescape.waiting.application.port.out;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import roomescape.waiting.application.port.out.projection.WaitingDetailProjection;
import roomescape.waiting.domain.Waiting;

public interface WaitingRepository {
    Waiting save(Waiting waiting);

    Optional<Waiting> findById(long waitingId);

    Optional<Waiting> findByIdForUpdate(long waitingId);

    Set<Long> findTimeIdByDateAndThemeId(LocalDate date, long themeId);

    boolean existsBySlotIdAndMemberId(long memberId, long slotId);

    boolean existsBySlotId(long slotId);

    List<Waiting> findAllBySlotIdOrderById(long slotId);

    List<Waiting> findAllBySlotIdOrderByIdForUpdate(long slotId);

    List<Waiting> findAllBySlotIds(List<Long> slotIds);

    List<WaitingDetailProjection> findAllWaitingDetails();

    List<WaitingDetailProjection> findAllWaitingDetailsByMemberId(long memberId);

    void deleteById(long waitingId);
}
