package roomescape.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.Schedule;
import roomescape.domain.Waiting;
import roomescape.domain.WaitingWithRank;

import java.util.List;
import java.util.Optional;

public interface JpaWaitingRepository extends JpaRepository<Waiting, Long> {

    @Query("""
        SELECT new roomescape.domain.WaitingWithRank(
            w, (SELECT COUNT(wr) + 1
                FROM Waiting wr
                WHERE wr.schedule.theme = w.schedule.theme
                    AND wr.schedule.date = w.schedule.date
                    AND wr.schedule.time = w.schedule.time
                    AND wr.createdAt < w.createdAt))
        FROM Waiting w
        WHERE w.member.id = :memberId
        ORDER BY w.schedule.date ASC, w.schedule.time.startAt ASC
    """)
    List<WaitingWithRank> findWaitingWithRankByMemberId(final Long memberId);

    boolean existsByMemberIdAndSchedule(Long memberId, Schedule schedule);

    Optional<Waiting> findTopByScheduleOrderByCreatedAt(Schedule schedule);
}
