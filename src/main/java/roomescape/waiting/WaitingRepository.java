package roomescape.waiting;

import roomescape.waiting.infrastructure.projection.WaitingDetailProjection;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface WaitingRepository {
    Waiting save(Waiting waiting);

    Optional<Waiting> findById(long waitingId);

    Optional<Waiting> findByIdForUpdate(long waitingId);

    Optional<Waiting> findFirstByScheduleId(long scheduleId);

    Optional<Waiting> findFirstByScheduleIdForUpdate(long scheduleId);

    boolean existsByScheduleIdAndMemberId(long scheduleId, long memberId);

    boolean existsByScheduleId(long scheduleId);

    long countByScheduleIdAndIdLessThanEqual(long scheduleId, long waitingId);

    void deleteById(long waitingId);

    List<WaitingDetailProjection> findUpcomingWaitingDetailsByMemberId(long memberId, LocalDateTime now);

    List<WaitingDetailProjection> findPastWaitingDetailsByMemberId(long memberId, LocalDateTime now);
}
