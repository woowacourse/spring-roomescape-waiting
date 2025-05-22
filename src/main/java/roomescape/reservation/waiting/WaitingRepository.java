package roomescape.reservation.waiting;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.schedule.Schedule;

import java.util.List;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    List<Waiting> findAll();

    @Query(value = """
                SELECT w FROM Waiting w
                WHERE w.schedule = :schedule
                AND w.rank > :rank
            """)
    List<Waiting> findWaitingGreaterThanRank(Schedule schedule, Long rank);

    List<Waiting> findAllBySchedule(Schedule schedule);
}
