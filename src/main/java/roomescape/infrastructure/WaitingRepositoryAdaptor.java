package roomescape.infrastructure;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import roomescape.domain.Waiting;
import roomescape.domain.repository.WaitingRepository;

@Repository
public class WaitingRepositoryAdaptor implements WaitingRepository {
    private final JpaWaitingRepository jpaWaitingRepository;

    public WaitingRepositoryAdaptor(final JpaWaitingRepository jpaWaitingRepository) {
        this.jpaWaitingRepository = jpaWaitingRepository;
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
    public List<Waiting> findByMemberId(final Long memberId) {
        return jpaWaitingRepository.findByMemberId(memberId);
    }
}
