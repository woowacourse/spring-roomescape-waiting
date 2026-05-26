package roomescape.waiting;

public interface WaitingRepository {
    Waiting save(Waiting waiting);

    boolean existsByScheduleIdAndMemberId(long memberId, long scheduleId);

    long countByScheduleIdAndIdLessThanEqual(long scheduleId, long waitingId);
}
