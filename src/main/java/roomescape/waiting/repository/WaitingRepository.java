package roomescape.waiting.repository;

import org.springframework.stereotype.Repository;
import roomescape.member.domain.Member;
import roomescape.schedule.domain.Schedule;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingWithRank;

import java.util.List;

@Repository
public interface WaitingRepository {
    Waiting save(Waiting waiting);

    List<WaitingWithRank> findWaitingWithRankByMemberId(Long memberId);

    void deleteById(Long waitingId);

    boolean existsByMemberAndSchedule(Member member, Schedule schedule);
}
