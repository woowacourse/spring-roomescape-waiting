package roomescape.repository.waiting;

import org.springframework.stereotype.Repository;
import roomescape.domain.waiting.Waiting;
import roomescape.domain.waiting.WaitingWithRank;

import java.util.List;
import java.util.Optional;

@Repository
public class WaitingRepositoryImpl implements WaitingRepository {

    private final JpaWaitingRepository jpaWaitingRepository;

    public WaitingRepositoryImpl(JpaWaitingRepository jpaWaitingRepository) {
        this.jpaWaitingRepository = jpaWaitingRepository;
    }

    @Override
    public long save(Waiting waiting) {
        return jpaWaitingRepository.save(waiting).getId();
    }

    @Override
    public Optional<Waiting> findById(Long id) {
        return jpaWaitingRepository.findById(id);
    }

    @Override
    public List<WaitingWithRank> findWaitingsWithRankByMemberId(Long memberId) {
        return jpaWaitingRepository.findWaitingsWithRankByMemberId(memberId);
    }

    @Override
    public boolean existsByDateAndTimeAndThemeAndMember(Waiting waiting) {
        return jpaWaitingRepository.existsByDateAndTimeAndThemeAndMember(waiting.getDate(), waiting.getTime(), waiting.getTheme(), waiting.getMember());
    }
}
