package roomescape.booking.waiting;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.booking.schedule.Schedule;
import roomescape.member.Member;

import java.util.List;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    @Query(value = """
                SELECT w FROM Waiting w
                WHERE w.schedule = :schedule
                AND w.rank > :rank
            """)
    List<Waiting> findWaitingGreaterThanRank(Schedule schedule, Long rank);

    List<Waiting> findAllBySchedule(Schedule schedule);

    List<Waiting> findAllByMember(Member member);

    Waiting findFirstByScheduleOrderByRankAsc(Schedule schedule);

    boolean existsBySchedule(Schedule schedule);
}
