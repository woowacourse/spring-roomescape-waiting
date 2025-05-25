package roomescape.waiting.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import roomescape.member.domain.Member;
import roomescape.schedule.domain.Schedule;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingWithRank;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface JpaWaitingRepository extends JpaRepository<Waiting, Long> {
    @Query("""
            SELECT new roomescape.waiting.domain.WaitingWithRank(
                            w,
                            (SELECT COUNT(w2)
                             FROM Waiting w2
                             WHERE w2.schedule.theme = w.schedule.theme
                               AND w2.schedule.date = w.schedule.date
                               AND w2.schedule.time = w.schedule.time
                               AND w2.id < w.id))
                        FROM Waiting w
                        WHERE w.member.id = :memberId
            """)
    List<WaitingWithRank> findWaitingWithRankByMemberId(Long memberId);

    boolean existsByMemberAndSchedule(Member member, Schedule schedule);

    boolean existsBySchedule(Schedule schedule);

    @Query("""
            SELECT w FROM Waiting w
            WHERE w.schedule.theme.id = :themeId
            AND w.schedule.date = :date
            AND w.schedule.time.startAt = :time
            ORDER BY w.id
            LIMIT 1
            """)
    Optional<Waiting> findFirstWaiting(@Param("themeId") Long themeId, @Param("date") LocalDate date, @Param("time") LocalTime time);
}
