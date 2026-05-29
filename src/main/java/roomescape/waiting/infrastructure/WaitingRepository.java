package roomescape.waiting.infrastructure;

import roomescape.waiting.Waiting;
import roomescape.waiting.infrastructure.projection.WaitingDetailProjection;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface WaitingRepository {
    Waiting save(Waiting waiting);

    Optional<Waiting> findById(long waitingId);

    boolean existsByScheduleIdAndMemberId(long scheduleId, long memberId);

    boolean existsByScheduleId(long scheduleId);

    long countByScheduleIdAndIdLessThanEqual(long scheduleId, long waitingId);

    void deleteById(long waitingId);

    List<WaitingDetailProjection> findUpcomingWaitingDetailsByMemberId(long memberId, LocalDateTime now);

    List<WaitingDetailProjection> findPastWaitingDetailsByMemberId(long memberId, LocalDateTime now);
}
