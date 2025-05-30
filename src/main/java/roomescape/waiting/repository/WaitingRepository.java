package roomescape.waiting.repository;

import roomescape.member.domain.Member;
import roomescape.schedule.domain.Schedule;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingWithRank;

import java.util.List;
import java.util.Optional;

public interface WaitingRepository {
    Waiting save(Waiting waiting);

    List<WaitingWithRank> findWaitingWithRankByMemberId(Long memberId);

    void deleteById(Long waitingId);

    boolean existsByMemberAndSchedule(Member member, Schedule schedule);

    List<Waiting> findAll();

    boolean existsBySchedule(Schedule schedule);

    Optional<Waiting> findById(Long id);

    Optional<Waiting> findFirstWaiting(Schedule schedule);
}
