package roomescape.persistence;

import org.springframework.stereotype.Repository;
import roomescape.domain.Schedule;
import roomescape.domain.Waiting;
import roomescape.domain.WaitingRepository;
import roomescape.domain.WaitingWithRank;

import java.util.List;
import java.util.Optional;

@Repository
public class WaitingRepositoryImpl implements WaitingRepository {

    private final JpaWaitingRepository jpaWaitingRepository;

    public WaitingRepositoryImpl(JpaWaitingRepository jpaWaitingRepository) {
        this.jpaWaitingRepository = jpaWaitingRepository;
    }

    @Override
    public List<WaitingWithRank> findWaitingWithRankByMemberId(final Long memberId) {
        return jpaWaitingRepository.findWaitingWithRankByMemberId(memberId);
    }

    @Override
    public Waiting save(Waiting waiting) {
        return jpaWaitingRepository.save(waiting);
    }

    @Override
    public Optional<Waiting> findById(final Long id) {
        return jpaWaitingRepository.findById(id);
    }

    @Override
    public void deleteById(final Long id) {
        jpaWaitingRepository.deleteById(id);
    }

    @Override
    public boolean existsByMemberIdAndSchedule(Long memberId, Schedule schedule) {
        return jpaWaitingRepository.existsByMemberIdAndSchedule(memberId, schedule);
    }

    @Override
    public List<Waiting> findAll() {
        return jpaWaitingRepository.findAll();
    }

    @Override
    public boolean existsById(final Long waitingId) {
        return jpaWaitingRepository.existsById(waitingId);
    }

    @Override
    public Optional<Waiting> findTopByScheduleOrderByCreatedAt(Schedule schedule) {
        return jpaWaitingRepository.findTopByScheduleOrderByCreatedAt(schedule);
    }
}
