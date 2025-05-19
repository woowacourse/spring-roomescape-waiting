package roomescape.waiting.infrastructure;

import java.util.Collection;
import org.springframework.stereotype.Repository;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingRepository;

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
    public Collection<Waiting> findAll() {
        return waitingJpaRepository.findAllWithEagerLoading();
    }
}
