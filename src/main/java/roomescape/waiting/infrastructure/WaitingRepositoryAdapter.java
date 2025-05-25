package roomescape.waiting.infrastructure;

import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingRepository;

@Repository
@AllArgsConstructor
public class WaitingRepositoryAdapter implements WaitingRepository {
    private final WaitingJpaRepository waitingJpaRepository;

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
    public Optional<Waiting> findById(Long id) {
        return waitingJpaRepository.findById(id);
    }
}
