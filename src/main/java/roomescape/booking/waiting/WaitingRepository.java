package roomescape.booking.waiting;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.booking.schedule.Schedule;
import roomescape.member.Member;

import java.time.LocalDateTime;
import java.util.List;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    List<Waiting> findAllByMember(Member member);

    Waiting findFirstByScheduleOrderByCreatedAtAsc(Schedule schedule);

    boolean existsBySchedule(Schedule schedule);

    Long countByScheduleAndCreatedAtLessThan(Schedule schedule, LocalDateTime createdAt);
}
