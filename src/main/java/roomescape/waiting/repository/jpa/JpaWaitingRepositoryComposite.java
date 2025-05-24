package roomescape.waiting.repository.jpa;

import org.springframework.stereotype.Repository;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingWithRank;
import roomescape.waiting.repository.WaitingRepository;

import java.util.List;

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
}
