package roomescape.waiting.repository.jpa;

import org.springframework.stereotype.Repository;
import roomescape.member.domain.Member;
import roomescape.schedule.domain.Schedule;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingWithRank;
import roomescape.waiting.repository.WaitingRepository;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaWaitingRepositoryComposite implements WaitingRepository {
    private final JpaWaitingRepository jpaWaitingRepository;

    public JpaWaitingRepositoryComposite(JpaWaitingRepository jpaWaitingRepository) {
        this.jpaWaitingRepository = jpaWaitingRepository;
    }

    @Override
    public Waiting save(Waiting waiting) {
        return jpaWaitingRepository.save(waiting);
    }

    @Override
    public List<WaitingWithRank> findWaitingWithRankByMemberId(Long memberId) {
        return jpaWaitingRepository.findWaitingWithRankByMemberId(memberId);
    }

    @Override
    public void deleteById(Long waitingId) {
        jpaWaitingRepository.deleteById(waitingId);
    }

    @Override
    public boolean existsByMemberAndSchedule(Member member, Schedule schedule) {
        return jpaWaitingRepository.existsByMemberAndSchedule(member, schedule);
    }

    @Override
    public List<Waiting> findAll() {
        return jpaWaitingRepository.findAll();
    }

    @Override
    public boolean existsBySchedule(Schedule schedule) {
        return jpaWaitingRepository.existsBySchedule(schedule);
    }

    @Override
    public Optional<Waiting> findById(Long id) {
        return jpaWaitingRepository.findById(id);
    }

    @Override
    public Optional<Waiting> findFirstWaiting(Schedule schedule) {
        return jpaWaitingRepository.findFirstWaiting(
                schedule.getTheme().getId(),
                schedule.getDate(),
                schedule.getTime().getStartAt()
        );
    }
}
