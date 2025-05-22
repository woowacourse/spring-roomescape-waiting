package roomescape.waiting.infrastructure;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingRepository;
import roomescape.waiting.domain.WaitingWithRank;

@Repository
public class WaitingRepositoryAdapter implements WaitingRepository {
    private final WaitingJpaRepository waitingJpaRepository;

    public WaitingRepositoryAdapter(WaitingJpaRepository waitingJpaRepository) {
        this.waitingJpaRepository = waitingJpaRepository;
    }

    @Override
    public Waiting save(Waiting waiting) {
        return waitingJpaRepository.save(waiting);
    }

    @Override
    public void deleteById(Long id) {
        waitingJpaRepository.deleteById(id);
    }

    @Override
    public List<Waiting> findAll() {
        return waitingJpaRepository.findAllWithEagerLoading();
    }

    @Override
    public List<WaitingWithRank> findWithRankByMemberId(Long memberId) {
        return waitingJpaRepository.findWithRankByMemberId(memberId);
    }

    @Override
    public Optional<WaitingWithRank> findWithRankById(Long id) {
        return waitingJpaRepository.findWithRankById(id);
    }

    @Override
    public Optional<Waiting> findById(Long id) {
        return waitingJpaRepository.findById(id);
    }
}
