package roomescape.waiting;

import roomescape.waiting.infra.projection.WaitingDetailProjection;

import java.util.List;
import java.util.Optional;

public interface WaitingRepository {
    Waiting save(Waiting waiting);

    Optional<Waiting> findById(long waitingId);

    boolean existsByScheduleIdAndMemberId(long memberId, long scheduleId);

    long countByScheduleIdAndIdLessThanEqual(long scheduleId, long waitingId);

    List<WaitingDetailProjection> findAllWaitingDetailsByMemberId(long memberId);

    void deleteById(long waitingId);
}
